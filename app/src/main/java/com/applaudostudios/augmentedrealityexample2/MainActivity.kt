package com.applaudostudios.augmentedrealityexample2

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.google.ar.core.*
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import kotlinx.android.synthetic.main.activity_main.*
import java.io.InputStream

class MainActivity : AppCompatActivity() {

    lateinit var aRFragment: CustomARFragment
    private var shouldAddPlane = true
    private var shouldAddXwing = true
    private lateinit var selectedObjectUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        aRFragment = supportFragmentManager.findFragmentById(customIrFragmentView.id) as CustomARFragment
        aRFragment.let { customARFragment ->
            customARFragment.planeDiscoveryController.hide()
            customARFragment.arSceneView.scene.addOnUpdateListener(this::onUpdateFrame)
        }

    }

    fun setAugmentedImageDb(config: Config, session: Session): Boolean {

        val augmentedImageDatabase = AugmentedImageDatabase(session)

        augmentedImageDatabase.addImage("airplane", loadAugmentedImages("airplane.jpg"))

        augmentedImageDatabase.addImage("xwing", loadAugmentedImages("xwing.jpg"))

        config.augmentedImageDatabase = augmentedImageDatabase

        return true
    }

    /***
     * Function to get the bitmap from a asset image
     */
    private fun loadAugmentedImages(imagePath: String): Bitmap {
        val assetManger = assets
        val inputStream: InputStream = assetManger.open(imagePath)
        return BitmapFactory.decodeStream(inputStream)
    }

    private fun onUpdateFrame(frameTime: FrameTime) {
        //Get the frame from the Fragment
        val aRFrame: Frame? = aRFragment.arSceneView.arFrame

        aRFrame?.let { frame ->
            val augmentedImages: Collection<AugmentedImage> = frame.getUpdatedTrackables(AugmentedImage::class.java)

            for (augmentedImage in augmentedImages) {
                //Check the augmentedImages state
                if (augmentedImage.trackingState == TrackingState.TRACKING && (shouldAddPlane || shouldAddXwing)) {
                        //when the Augmanted Images is tracking
                        Log.d("LOL", "Tracking")
                        when (augmentedImage.name) {
                            "airplane" -> {
                                setModelPath("Airplane.sfb")
                                placeObject(
                                    aRFragment,
                                    augmentedImage.createAnchor(augmentedImage.centerPose),
                                    selectedObjectUri
                                )
                                shouldAddPlane = false2
                            }
                            "xwing" -> {
                                setModelPath("model_X Wing_20171214_155110102.sfb")
                                placeObject(
                                    aRFragment,
                                    augmentedImage.createAnchor(augmentedImage.centerPose),
                                    selectedObjectUri
                                )
                                shouldAddXwing = false
                            }
                        }
                    }

            }
        }
    }

    /***
     * function to handle the renderable object and place objecto in scene
     */
    private fun placeObject(fragment: ArFragment, anchor: Anchor, modelUri: Uri) {
        val modelRenderable = ModelRenderable.builder()
            .setSource((fragment.requireContext()), modelUri)
            .build()
            .thenAccept { renderableObject -> addNodeToScene(fragment, anchor, renderableObject) }
            .exceptionally {
                val toast = Toast.makeText(applicationContext, "Error", Toast.LENGTH_SHORT)
                toast.show()

                null
            }
    }

    /***
     * Function to a child anchor to a new scene.
     */
    private fun addNodeToScene(fragment: ArFragment, anchor: Anchor, renderableObject: Renderable) {
        val anchorNode = AnchorNode(anchor)
        val transformableNode = TransformableNode(fragment.transformationSystem)
        transformableNode.renderable = renderableObject
        transformableNode.setParent(anchorNode)
        fragment.arSceneView.scene.addChild(anchorNode)
        transformableNode.select()
    }

    /***
     * function to get the model resource on assets directory for each figure.
     */
    private fun setModelPath(modelFileName: String) {
        selectedObjectUri = Uri.parse(modelFileName)
    }
}
