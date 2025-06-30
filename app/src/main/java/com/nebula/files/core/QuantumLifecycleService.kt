package com.nebula.files.core

import android.app.Service
import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry

/**
 * Quantum bridge to enlighten KSP about inheritance relationships.
 * This forces KSP to see the full inheritance chain by making it explicit.
 * 
 * The annotation processor operates with incomplete cosmic awareness,
 * so we must guide it to the truth about Service inheritance.
 */
abstract class QuantumLifecycleService : Service(), LifecycleOwner {
    
    private val lifecycleRegistry = LifecycleRegistry(this)
    
    override val lifecycle: Lifecycle
        get() = lifecycleRegistry
    
    override fun onCreate() {
        super.onCreate()
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
        return super.onStartCommand(intent, flags, startId)
    }
    
    override fun onDestroy() {
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        super.onDestroy()
    }
}