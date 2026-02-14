package com.aniper.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aniper.MainActivity
import com.aniper.R
import com.aniper.data.LocalPetData
import com.aniper.model.Pet
import com.aniper.overlay.PetOverlayService
import com.google.android.material.floatingactionbutton.FloatingActionButton

class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.rv_pets)
        emptyView = view.findViewById(R.id.tv_empty)

        val pets = LocalPetData.samplePets

        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerView.adapter = PetAdapter(pets) { pet -> onPetClicked(pet) }

        emptyView.visibility = if (pets.isEmpty()) View.VISIBLE else View.GONE
        recyclerView.visibility = if (pets.isEmpty()) View.GONE else View.VISIBLE

        view.findViewById<FloatingActionButton>(R.id.fab_settings).setOnClickListener {
            YRangeSettingsDialog(requireContext()).show()
        }

        view.findViewById<FloatingActionButton>(R.id.fab_stop_overlay).setOnClickListener {
            (activity as? MainActivity)?.stopOverlayService()
            Toast.makeText(requireContext(), "Overlay stopped", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onPetClicked(pet: Pet) {
        val activity = requireActivity() as MainActivity
        if (!activity.hasOverlayPermission()) {
            activity.requestOverlayPermission()
            return
        }

        PetOverlayService.start(requireContext(), pet.id)
        Toast.makeText(requireContext(), "${pet.name} is now on your screen!", Toast.LENGTH_SHORT).show()
    }

    inner class PetAdapter(
        private val pets: List<Pet>,
        private val onClick: (Pet) -> Unit
    ) : RecyclerView.Adapter<PetAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val thumbnail: ImageView = view.findViewById(R.id.iv_pet_thumbnail)
            val name: TextView = view.findViewById(R.id.tv_pet_name)
            val status: TextView = view.findViewById(R.id.tv_pet_status)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_pet, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val pet = pets[position]
            val asset = LocalPetData.getAssetById(pet.assetId)

            holder.name.text = pet.name
            holder.status.text = if (pet.isActive) "Tap to activate!" else "Inactive"
            holder.thumbnail.setImageDrawable(
                ContextCompat.getDrawable(holder.itemView.context, asset.idleRightRes)
            )
            holder.itemView.setOnClickListener { onClick(pet) }
        }

        override fun getItemCount() = pets.size
    }
}
