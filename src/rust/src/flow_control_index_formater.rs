use radix_fmt::radix;
use std::time::SystemTime;

static BASE10_CHAR_ARRAY: &str = &"ABCDEFGHIJKLMNOPQRSTUVWXYZ";
static BASE10_RADIX: usize = 10;

struct FlowControlIndexFormatter {
    char_array: Vec<char>,
    radix: usize,
    last_char: String,
}

impl FlowControlIndexFormatter {
    pub fn create(radix: usize, char_array: Vec<char>) -> Self {
        let last_char = String::from(char_array[char_array.len() - 1]);

        FlowControlIndexFormatter {
            char_array,
            radix,
            last_char,
        }
    }

    pub fn of_base(radix: usize, human_readable: bool) -> Result<Self, String> {
        let char_array: Vec<char> = (if human_readable {
            BASE10_CHAR_ARRAY.replace("O", "").replace("I", "")
        } else {
            String::from(BASE10_CHAR_ARRAY)
        })
        .chars()
        .collect();

        if radix > BASE10_RADIX && radix < char_array.len() {
            return Ok(Self::create(radix, Vec::from(&char_array[0..])));
        }

        if radix > 0 && radix <= BASE10_RADIX {
            return Ok(Self::create(radix, char_array));
        }

        Err("Unsupported radix or character set size.".into())
    }

    pub fn max_number_by_length(&self, length: u32) -> usize {
        if length < 1 {
            return 0;
        }

        let base_range = self.radix.pow(length);
        let mut max_value = base_range - 1;

        let mut segment_size = base_range;

        loop {
            segment_size /= self.radix;
            if segment_size <= 0 {
                break;
            }

            max_value += self.char_array.len() * segment_size;
        }

        max_value
    }

    pub fn format_index(&self, value: usize, length: u32) -> Result<String, String> {
        if length < 1 {
            return Err("Length must be at least 1.".into());
        }

        let mut numeric_limit = self.radix.pow(length);
        if numeric_limit > value {
            return Ok(self.format_radix_string(value, length));
        }

        let mut extended_limit = numeric_limit - 1;
        let mut overflow_tier = 0;

        loop {
            numeric_limit /= self.radix;
            if numeric_limit <= 0 {
                break;
            }

            extended_limit += self.char_array.len() * numeric_limit;

            if extended_limit >= value {
                let previous_tier_start = extended_limit - (self.char_array.len() * numeric_limit);
                return Ok(self.assemble_code(
                    value,
                    length as usize,
                    overflow_tier,
                    previous_tier_start,
                    numeric_limit,
                ));
            }

            overflow_tier += 1;
        }

        Err("Value too large for given length.".into())
    }

    fn assemble_code(
        &self,
        value: usize,
        length: usize,
        overflow_tier: usize,
        previous_tier_start: usize,
        numeric_limit: usize,
    ) -> String {
        let mut builder = String::with_capacity(length);
        builder.push_str(&self.last_char.repeat(overflow_tier));
        let size = ((value - (previous_tier_start + 1)) / numeric_limit) as usize;
        let c = self.char_array[size];
        builder.push(c);

        if numeric_limit > 1 {
            let pad = numeric_limit.ilog(self.radix);
            builder.push_str(&self.format_radix_string(value % numeric_limit, pad));
        }
        builder
    }

    pub fn format_radix_string(&self, value: usize, length: u32) -> String {
        let s = radix(value, self.radix as u8).to_string();
        if s.len() >= length as usize {
            return s;
        }

        let mut pad = "0".repeat(length as usize - s.len());
        pad.push_str(&s);
        pad
    }
}

pub fn test(len: u32) {
    if let Ok(fci) = FlowControlIndexFormatter::of_base(10, true) {
        let max = fci.max_number_by_length(len);
        let mut codes: Vec<String> = Vec::with_capacity(max + 1);
        let started_at = SystemTime::now();

        for i in 1..=max {
            if let Ok(code) = fci.format_index(i, len) {
                // codes.push(code)
            } else {
                panic!("Failed")
            }
        }

        if let Ok(elapsed) = started_at.elapsed() {
            println!("Generated {} codes in {} ms.", codes.len(), elapsed.as_millis());
        }
    }
}
