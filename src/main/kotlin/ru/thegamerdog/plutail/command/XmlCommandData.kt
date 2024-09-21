package ru.thegamerdog.plutail.command

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlCData
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import nl.adaptivity.xmlutil.serialization.XmlValue

@Serializable
@XmlSerialName(value = "u")
data class UserDataXml(
    val i: Int,
    //val p: Int,

    val m: Int,
    val s: Int,

    @XmlCData
    @XmlElement
    val n: String,
    val vars: VarsXml
)

@Serializable
@XmlSerialName(value = "user")
data class UserIdXml(
    val id: Int
)

@Serializable
@XmlSerialName(value = "vars")
data class VarsXml(
    val `var`: ArrayList<Var>? = null
)

@Serializable
@XmlSerialName(value = "obj")
data class Obj(
    val t: String,
    val o: String,
    val `var`: ArrayList<Var>? = null
)

@Serializable
@XmlSerialName(value = "var")
data class Var(
    val n: String,
    val t: String,

    @XmlValue
    val value: String
)

@Serializable
@XmlSerialName(value = "dataObj")
data class DataObj(
    val obj: ArrayList<Obj>? = null,
    val `var`: ArrayList<Var>? = null
)

@Serializable
@XmlSerialName(value = "body")
data class BodyXml(
    val action: String,
    val r: Int,

    @XmlCData
    @XmlValue
    val dataObj: String? = null
)

@Serializable
@XmlSerialName(value = "msg")
data class MsgXml (
    val t: String,
    val body: BodyXml
)