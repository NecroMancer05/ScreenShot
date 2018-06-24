package com.bekircan.ss

import org.jnativehook.GlobalScreen
import org.jnativehook.NativeHookException
import org.jnativehook.keyboard.NativeKeyEvent
import org.jnativehook.keyboard.NativeKeyListener

import javax.imageio.ImageIO
import java.awt.*
import java.io.*
import java.text.SimpleDateFormat
import java.util.Calendar

@Suppress("NAME_SHADOWING")
class Main : NativeKeyListener {

    override fun nativeKeyTyped(nativeKeyEvent: NativeKeyEvent) {

    }

    override fun nativeKeyPressed(nativeKeyEvent: NativeKeyEvent) {

        if (nativeKeyEvent.keyCode == NativeKeyEvent.VC_F12) {

            takeScreenShot()
        }
    }

    override fun nativeKeyReleased(nativeKeyEvent: NativeKeyEvent) {

    }

    companion object {

        private var savePath : String = ""

        @JvmStatic
        fun main(args: Array<String>) {

            if (!SystemTray.isSupported()) {

                println("System  tray not supported !")
            }

            createDocumentsSetting()

            val icon = iconPath()

            val trayIconImage = Toolkit.getDefaultToolkit().getImage(icon)

            val popupMenu = PopupMenu()
            val trayIcon = TrayIcon(trayIconImage, "Screen Shot", popupMenu)
            val systemTray = SystemTray.getSystemTray()

            val takeScreenShot = MenuItem("Take Screen Shot")

            takeScreenShot.addActionListener { takeScreenShot() }

            val exitItem = MenuItem("Exit")

            exitItem.addActionListener { System.exit(1) }

            popupMenu.add(takeScreenShot)
            popupMenu.add(exitItem)

            trayIcon.popupMenu = popupMenu

            try {
                systemTray.add(trayIcon)
            } catch (e: AWTException) {
                e.printStackTrace()
                println("Tray not added")
            }

            try {
                GlobalScreen.registerNativeHook()
            } catch (e: NativeHookException) {
                e.printStackTrace()
            }

            GlobalScreen.addNativeKeyListener(Main())
        }


        private fun takeScreenShot() {

            try {
                val savePath = File(savePath)

                if (!savePath.exists()) {

                    savePath.mkdirs()
                }

                val header = SimpleDateFormat("HH.MM.ss - dd.MM.YYYY").format(Calendar.getInstance().time)
                val bufferedImage = Robot().createScreenCapture(Rectangle(Toolkit.getDefaultToolkit().screenSize))
                ImageIO.write(bufferedImage, "png", File(savePath.toString() +"\\"+ header +".png"))

            } catch (e: AWTException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        private fun getUserDocumentsPath() : String{

            var myDocuments = ""
            try {
                val p = Runtime.getRuntime().exec("reg query \"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\Shell Folders\" /v personal")
                p.waitFor()
                val `in` = p.inputStream
                val b = ByteArray(`in`.available())
                `in`.read(b)
                `in`.close()
                myDocuments = String(b)
                myDocuments = myDocuments.split(("\\s\\s+").toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[4]
            }
            catch (e:Exception) {
                e.printStackTrace()
            }

            return myDocuments
        }

        private fun createDocumentsSetting(){

            val settingsPath = getUserDocumentsPath() + "\\ScreenShot\\Setting.txt"
            val path = getUserDocumentsPath() + "\\ScreenShot"


            val directory = File(path)
            val settingsDirectory =  File(settingsPath)

            if (directory.exists() && settingsDirectory.exists()){

                readSetting()
            }else{

                if (!directory.exists()){

                    directory.mkdir()
                }

                if (!settingsDirectory.exists()) {
                    val writer = PrintWriter(settingsPath, "UTF-8")
                    writer.print(System.getProperty("user.home") + "\\Desktop") //setDefault as desktop path
                    writer.close()
                    readSetting()
                }
            }
        }

        private fun readSetting(){

            val path = getUserDocumentsPath() + "\\ScreenShot\\Setting.txt"
            val reader = BufferedReader(FileReader(path))

            reader.use { reader ->
                val stringBuilder = StringBuilder()
                var line = reader.readLine()

                while (line != null){
                    stringBuilder.append(line)
                    stringBuilder.append(System.lineSeparator())
                    line = reader.readLine()
                }
                savePath = stringBuilder.toString().replace("\n", "").replace("\r", "")
                print("save" + savePath + "yan")
            }
        }

        private fun iconPath() : String{

            return getUserDocumentsPath() + "\\ScreenShot\\icon.png"
        }


    }
}
