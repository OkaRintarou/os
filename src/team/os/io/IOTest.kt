package team.os.io

import team.os.global.Global

fun main() {
    with(Global.io) {
        println(test0.facilityTotalNumberChart)
        println(test0.stateChart)
        val str = "123456"
        val l1 = IOFacilityRequest("keyboard", str.length, str)
        println(l1)
        val l2 = IOFacilityRequest("keyboard", str.length, str)
        println(l2)
        val l3 = IOFacilityRequest("keyboard", str.length, str)
        println(l3)
        val l4 = IOFacilityRequest("keyboard", str.length, str)
        println(l4)
        println(test0.stateChart)
    }
}
