import scala.scalajs.js
import org.scalajs.dom
import dom.document

object ScalaJSExample extends js.JSApp {

  def appendPar(targetNode: dom.Node, text: String): Unit = {
    val parNode = document.createElement("p")
    val textNode = document.createTextNode(text)
    parNode.appendChild(textNode)
    targetNode.appendChild(parNode)
  }

  def main(): Unit = {
    // Only print to console
    println("Hello world!")
    // Really change HTML
    appendPar(document.body, "Hello World")
    // Add values to li element
    val parNode = document.getElementById("tickets")
    val textNode = document.createTextNode("School MyEdu")
    parNode.appendChild(textNode)
    appendPar(parNode, "Hello World")
  }
}
