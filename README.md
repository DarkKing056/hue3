# Markdown Complete Guide

## Headers
# H1
## H2
### H3
#### H4
##### H5
###### H6

---

## Emphasis
*Italic*, **Bold**, ***Bold Italic***  
_Italic_, __Bold__, ___Bold Italic___

~~This text is strikethrough~~


## Custom Header

> Level 1
>> Level 2
>>> Level 3
>>>> Level 4
---

## Lists
### Ordered
1. First
2. Second
3. Third

### Unordered
- Item A
- Item B
  - Sub-item B1
  - Sub-item B2

### Task Lists
- [x] Task 1
- [ ] Task 2

---

## Links and Images
[Link Text](https://example.com)  
![Alt Text](https://upload.wikimedia.org/wikipedia/commons/4/48/Markdown-mark.svg)

---

## Code
Inline code: `print("Hello")`  
Block code:
```csharp
using System;
class Program
{
    static void Main(string[] args)
    {
        PrintTheCode();
    }

    private void PrintTheCode(){
        string h="Hello World";
        Console.WriteLine(h);
    }
}

/*TODO ahaha*/
```
---

---

| Product Name | Price  | In Stock |
|--------------|--------|----------|
| Laptop       | $999   | Yes      |
| Smartphone   | $699   | No       |
| Tablet       | $399   | Yes      |


[Go to Custom Header](#custom-header)


<h2 id="custom-id">Custom Header</h2>


[Go to Custom Header](#custom-id)