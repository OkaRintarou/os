package team.os.io

import team.os.global.Global

fun main() {
    with(Global.io) {
        println(test0.facilityTotalNumberChart)
        println(test0.stateChart)
        val str = "123456"
        lateinit var list: List<Int>
        for (i in 1..6) {
            list = IOFacilityRequest("printer", str.length, str)
        }
        IOFacilityRelease("printer", list[0])
        val l = IOFacilityRequest("printer", str.length, str)
        println(list)
        println(l)
        println(test0.stateChart)
    }
}
