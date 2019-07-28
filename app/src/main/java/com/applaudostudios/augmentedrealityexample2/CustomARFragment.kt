package com.applaudostudios.augmentedrealityexample2

import android.util.Log
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.sceneform.ux.ArFragment


class CustomARFragment : ArFragment() {

    override fun getSessionConfiguration(session: Session?): Config {

        planeDiscoveryController.setInstructionView(null)
        val config = Config(session)
        config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
        session?.configure(config)

        arSceneView.setupSession(session)
        session?.let { safeSession ->
            if ((requireActivity() as MainActivity).setAugmentedImageDb(config, safeSession)) {
                Log.d("SetUpAugImageDb", "Success")
            } else {
                Log.e("SetUpAugImageDb", "Failure")
            }
        }
        return config
    }

}