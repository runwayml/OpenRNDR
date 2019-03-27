
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONArray
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import java.net.URISyntaxException

import org.json.JSONObject
import org.openrndr.Program
import org.openrndr.ffmpeg.ScreenRecorder


fun main() = application {

    val connections:List<Pair<String, String>> = listOf(
        Pair("rightHip", "leftHip"),
        Pair("rightHip", "rightShoulder"),
        Pair("leftHip", "leftShoulder"),
        Pair("leftShoulder", "rightShoulder"),
        Pair("leftShoulder", "leftElbow"),
        Pair("leftElbow", "leftWrist"),
        Pair("rightShoulder", "rightElbow"),
        Pair("rightElbow", "rightWrist"),
        Pair("leftShoulder", "leftElbow"),
        Pair("rightHip", "rightKnee"),
        Pair("rightKnee", "rightAnkle"),
        Pair("leftHip", "leftKnee"),
        Pair("leftKnee", "leftAnkle")
    )

    //  These are the standard window size but elements are rendered in line with the source size / coordinates.
    //  i.e. if  you are using a 1024x768 source, then OpenRNDR may render elements outside of this window size and
    //  it should be adjusted with this in mind
    configure {
        width = 640
        height = 480
    }

    program {

        // extend(ScreenRecorder())

        var json = JSONObject()
        val mSocket: Socket
        val onNewMessage = object: Emitter.Listener {
            override fun call(vararg args: Any?) {
                json = args[0] as JSONObject
            }
        }

        // These are the current defaults - it's worth checking your own port settings
        try {
            mSocket = IO.socket("http://localhost:3001/")
            mSocket.on("data", onNewMessage)
            mSocket.connect()
        } catch (e: URISyntaxException) {
            println("error")
        }

        extend {
            drawer.stroke = ColorRGBa.PINK
            drawer.strokeWeight = 1.0
            drawer.fill = ColorRGBa.PINK

            if (json.has("poses")) {
                val humans: JSONArray = json.getJSONArray("poses")

                    // For each human found
                    for (j in 0..(humans.length() - 1)) {
                        val pose: JSONObject = humans.getJSONObject(j)
                        var bodyparts: JSONArray = pose.getJSONArray("keypoints")

                        drawBodyParts(bodyparts)
                        for (connection in connections) {
                            var start: JSONObject? = null
                            var end: JSONObject? = null

                            for (i in 0..(bodyparts?.length() - 1)) {
                                val startBodypart = bodyparts.getJSONObject(i)
                                if (startBodypart.getString("part") == connection.first) {
                                    start = startBodypart
                                    for (j in 0..(bodyparts?.length() - 1)) {
                                        val endBodypart = bodyparts.getJSONObject(j)
                                        if (endBodypart.getString("part") == connection.second) {
                                            end = endBodypart
                                            break
                                        }
                                    }
                                    break
                                }
                            }

                            if (start != null && end != null) {
                                drawer.lineSegment(
                                    start.getJSONObject("position").getDouble("x"),
                                    start.getJSONObject("position").getDouble("y"),
                                    end.getJSONObject("position").getDouble("x"),
                                    end.getJSONObject("position").getDouble("y"))
                            }
                        }
                    }
            }
        }
    }
}


private fun Program.drawBodyParts(bodyparts: JSONArray) {
    for (i in 0..(bodyparts.length() - 1)) {
        val bodypart = bodyparts.getJSONObject(i).getJSONObject("position")
        if (bodypart != null)
            drawer.circle(bodypart.getDouble("x"), bodypart.getDouble("y"), 3.0)
    }
}