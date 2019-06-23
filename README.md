## Subscm

### 简介

Subscm 是一个 Lisp 系语言，文法规则是目前已有的 scheme 语言的子集，『sub-scheme』一词有这层含义。进一步缩写后的名称为『Subscm』。

Subscm 支持计算、条件选择、变量定义和函数调用等功能，能够表达基本的算法。以下是部分使用例子：

1. 定义变量 x = 256
```scheme
(define x 256)
```
2. 加法
```scheme
(+ 1 2 3) ; 返回结果 6
```
3. 定义函数：两数平方和，并计算 3 和 4 的平方和
```scheme
(define square (lambda (x) (* x x)))
(define square-sum (lambda (x y) (+ (square x) (square y))))
(square-sum 3 4) ; 返回结果 25
```
4. 定义包含 1，2，3 三个元素的列表，变量名为 lst
```scheme
(define lst (list 1 2 3))
```
5. 判断两个数 x y 是否相等
```scheme
(define x 1)
(define y 2)
(eq? x y) ; 返回结果 false
```
6. 计算列表 lst 的长度
```scheme
(define len (lambda (lst) 
  (if (eq? lst (list))
    0 
    (+ 1 (len (cdr lst))))
))
(define my-list (list 1 2 3))
(len my-list)  ; 返回结果 3
```

### 解析-求值

为了实现上面例子的功能，每个程序片段都需要经历两个阶段：解析和求值。

解析：根据 __文法规则__ 将一个字符串『拆解』成一个个组成部分，使它们成为富有意义的数据结构，也就是语法树，便于进一步分析或计算求值。

求值：对语法树进行规约（reduce），即将一颗树转换为一个值。对于不同类型的树节点，都予以不同的求值方式计算，这就需要用到 __求值规则__。

相应的，有两个关键的组件来完成这些任务，分别是解析器（Parser）和求值组件（Evaluator）。它们的实现将在后面介绍。下面来看具体的文法规则和求值规则。

### 文法规则
Subscm 的文法规则十分简单，只有六条规则：

```
Program => Expr*
Expr 	=> Int | Bool | Symbol | List
Int 	=> [0-9]+
Bool 	=> true | false
Symbol	=> Char+
List 	=> (Expr*)
```
- 程序（Program）由 0 个或多个表达式（Expr）组成，对一个 Program 的求值结果为最后一个表达式的求值结果。
- 表达式有四种：整数（Int）、布尔值（Bool）、符号（Symbol）和列表（List）。
- Int 由一个或多个数字组成；Bool 由 `true` 和 `false` 组成；Symbol 由一个或多个非空格且非圆括号的字符组成。
- List 由一对圆括号夹着一串Expr组成。
List 是函数和控制结构的基础，根据 List 中的第一个 Symbol 或关键字，可以使用特定的规则进行求值。具体参考后面的『求值规则』。

这六条规则只定义了类似于广义表的语法，仅仅是规定了程序的形状。大部分功能例如条件判断、函数和变量定义并没有在这里体现，而是统一解析为 List，在求值的时候实现。

### 环境变量
在介绍求值规则之前，先讨论环境变量。

为什么需要环境变量？回顾简介中的几个例子，可以发现多个表达式之间是有联系的，后面的表达式能够使用前面 `define` 表达式定义的变量。

文法规则的生产式 Program 一条也定义了，每个程序都是由表达式组成的。为了让在 __前面__ 求值的表达式对 __后面__ 的表达式造成影响（即实现变量定义），在求值的过程中，需要将 `define` 的结果保存到一个全局的状态，这个状态就是环境变量。

例如 `(define x 1)`，就是将 `x => 1` 这个键值对保存到环境中，在以后求值遇到 `x` 时可以直接替换为 `1` 。例如 `(eq? x 1)` 求值遇到 `x`，替换为 `1`，然后再判断相等，返回 `true`。

### 求值规则


| 表达式     | 值                                                           |
| ---------- | ------------------------------------------------------------ |
| Expr       | 需要动态判断其准确类型（Symbol / Bool / Int / List）来递归求值 |
| Bool / Int | 与表达式的值一致                                             |
| Symbol     | 从环境变量中寻找，如果找到，返回结果值；否则抛出错误『未定义符号』 |
| List       | 根据内部一串表达式中的第一个 Symbol 或 keyword 来 switch     |

下面考察 List 中第一项表达式。首先，它必须是 Symbol 类型的。原因是在 Subscm 中，左括号紧跟着的表达式要不是 __函数名__ ，要不是 __关键字__ （例如用于条件选择的 `if`，用于函数定义的 `lambda`），如果都不是，抛出一个解析错误。

| List 子表达式串的第一个表达式 | 值（lst为子表达式列表，lst[0]表示第一项，依此类推）          |
| ----------------------------- | ------------------------------------------------------------ |
| define                        | lst长度必须为3，其中 lst[1] 和 lst[2] 分别作为键和值存到环境 |
| lambda                        | lst长度必须为3，其中 lst[1] 和 lst[2] 分别作为函数参数和函数体，return一个函数 |
| if                            | lst长度必须为4，其中 lst[1] 为谓词，lst[2] 和 lst[3]分别为条件成立和不成立时，需要求值的表达式。当 lst[1] 为 true 时，return lst[2] 的求值结果；否则return lst[3] 的求值结果。 |
| list                          | lst长度无限制，return 每个表达式求值后的整个除 lst[0] 以外的列表 |
| *other*                       | 其他都当作函数调用，函数名为 lst[0]，值从环境中获取，并把 lst[1..] 作为实参列表 |

### 解析器

解析器的实现参考了 parser combinator 的思想，首先 Parser 只要求有一个接口，即 parse，能够接受一个字符串，返回解析结果 ParseResult。

ParseResult 也是接口，一共有三个实现：
- ParseSuccess，包含成功解析的语法树和剩余未解析的字符串。
- ParseError，包含解析错误的原因。
- ParsePending，既非成功，也非失败，表示解析了一串表达式。

Parser 的实现有很多种，每种都对应了文法规则中一条生产式的终结符或非终结符。

为了像文法规则一样，多个符号能够规约成一个非终结符，多个解析器也要组合成一个解析器，只给上层提供一个parse接口，返回一个ParseResult。那么，应该如何组合呢？

符号的组合无非有两种：顺序解析（sequence）和选择解析（alternative）。上述文法规则中，顺序解析的例子是`List 	=> ( Expr* )`，选择解析的例子是 `Expr => Int | Bool | Symbol | List  ` 。

- 对于顺序解析，复合解析器只需要顺序的调用子解析器的 parse 接口，收集它们的结果，聚集为自己的解析结果返回出去即可。
- 对于选择解析，复合解析器需要逐个调用子解析器，抛弃产生解析错误的，留下解析正确的。如果正确的只有一个，返回它的解析结果出去；如果没有正确的，或者正确的有多个（文法规则中存在左公因子）也应该抛出解析错误。

> 打破循环依赖：如果将解析器作为对象，在构建组合子的时候可能会产生循环依赖。例如构建 Expr 解析器需要一个 List 解析器，而要构建 List 解析器又需要一个 Expr 解析器，无限循环。这里的实现采用了一个临时的方案：将 Expr 所代表的选择解析器的依赖从一个 __实体对象__ 转变为 __实体对象工厂函数__ 。简单来说就是不依赖 `Parser`，而依赖于 `() -> ListParser`。将子解析器的构建推迟到求值期进行，打破了循环构建。

### 求值组件

项目中的求值组件是一个类，只提供了 eval 静态方法，接受解析完的语法树和环境变量，返回求值结果。求值的行为按照上述的『求值规则』来实现。

> 没有模式匹配的语言不是好语言。—— Scala 使用者

