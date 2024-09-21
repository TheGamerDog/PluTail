package ru.thegamerdog.plutail.util

import ru.thegamerdog.plutail.command.Obj
import ru.thegamerdog.plutail.command.Var

object DataObjTransform {
    fun objToDataObj(name: String, value: MutableMap<String, Any?>): Obj {
        val listOfParams: ArrayList<Var> = paramsToDataObj(value)

        return Obj(
            t = "o",
            o = name,
            `var` = listOfParams
        )
    }

    fun arrayToDataObj(name: String, values: ArrayList<Any?>): Obj {
        val listOfParams: ArrayList<Var> = arrayListOf()

        values.forEachIndexed { index, value ->
            val type = when (value) {
                is String -> "s"
                is Int -> "n"
                is Boolean -> "b"
                else -> "x"
            }

            listOfParams.add(
                Var(
                    n = index.toString(),
                    t = type,
                    value = value.toString()
                )
            )
        }

        return Obj(
            t = "a",
            o = name,
            `var` = listOfParams
        )
    }

    fun paramsToDataObj(value: MutableMap<String, Any?>): ArrayList<Var> {
        val listOfParams: ArrayList<Var> = arrayListOf()

        value.forEach {
            val type = when (it.value) {
                is String -> "s"
                is Int, is Float -> "n"
                is Boolean -> "b"
                else -> "x"
            }

            listOfParams.add(
                Var(
                    n = it.key,
                    t = type,
                    value = it.value.toString()
                )
            )
        }

        return listOfParams
    }
}