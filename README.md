# Flow Control Index Formatter

> Code formatting, under control.  
> Dynamic overflow indexing, fixed-length layout.  

---

## 🧭 What is this?

You want fixed-length codes.  
You want them to overflow gracefully.  
You want them fast.  
This does that.

Supports numeric to alphanumeric overflow like:
```
… → 998
999 → A00
A99 → Z99
Z99 → ZA0
```
---

## ⚙️ Features

- Radix-based indexing (e.g., base 10, 16, 32, etc.)
- Human-readable mode (skips 'O', 'I')
- Fully dynamic code overflow handling
- Consistent fixed-width format
- Microsecond-level performance

---

## ✨ Example

```java
FlowControlIndexFormatter fci = FlowControlIndexFormatter.ofBase(10, true);
System.out.println(fci.formatIndex(999, 3));  
System.out.println(fci.formatIndex(2599, 3)); 