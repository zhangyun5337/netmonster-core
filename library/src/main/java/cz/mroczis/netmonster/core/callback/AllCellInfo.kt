package cz.mroczis.netmonster.core.callback

import cz.mroczis.netmonster.core.model.cell.ICell
import cz.mroczis.netmonster.core.model.model.CellError

//这段代码是一个包声明，并引入了两个类型别名CellCallbackSuccess和CellCallbackError。
//这段代码引入了cz.mroczis.netmonster.core.model.cell.ICell和cz.mroczis.netmonster.core.model.model.CellError两个类。
//CellCallbackSuccess是一个类型别名，指定了一个函数类型。该函数类型接收一个List<ICell>类型的参数，并且没有返回值。
//CellCallbackError也是一个类型别名，指定了另一个函数类型。该函数类型接收一个CellError类型的参数，并且没有返回值。
//这些类型别名很可能用于定义回调函数，以处理成功和失败的情况。
//输出结果：无输出

typealias CellCallbackSuccess = (cells: List<ICell>) -> Unit
typealias CellCallbackError = (error: CellError) -> Unit
