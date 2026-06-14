package com.example.data

data class CloneInfo(
    val id: String,
    val name: String,
    val series: String,
    val yearIntroduced: String,
    val yieldPotential: String, // kg/ha/tahun
    val diseaseResistance: String,
    val suggestedArea: String,
    val description: String,
    val soilSuitability: String, // info untuk smart-recommendation
    val annualRainfallNeeded: String, // e.g., "1800 - 2500 mm"
    val maxElevation: String, // e.g. "300m"
    val typicalConfidence: Float = 0.92f
) {
    companion object {
        val defaultClones = listOf(
            CloneInfo(
                id = "rrim_2025",
                name = "RRIM 2025",
                series = "RRIM 2000 Series",
                yearIntroduced = "2003",
                yieldPotential = "2,500 - 3,000 kg/ha/tahun",
                diseaseResistance = "Sangat Tinggi (Rintang Corynespora & Oidium)",
                suggestedArea = "Sesuai untuk sebahagian besar kawasan Semenanjung Malaysia (Zon A & B)",
                description = "Klon getah lateks-balak yang sangat popular kerana pertumbuhan pantas, kulit kayu tebal, dan potensi menghasilkan balak getah berkualiti tinggi di samping hasil susu getah yang lebat.",
                soilSuitability = "Tanah Lempung Pasir (Sandy Clay) & Tanah Lateral Kelikir",
                annualRainfallNeeded = "2,000 - 2,800 mm",
                maxElevation = "350 meter"
            ),
            CloneInfo(
                id = "pb_260",
                name = "PB 260",
                series = "Prang Besar Series",
                yearIntroduced = "1983",
                yieldPotential = "2,000 - 2,500 kg/ha/tahun",
                diseaseResistance = "Sederhana (Ulat Daun & Phytophthora)",
                suggestedArea = "Kawasan beralun ringan dan zon kering Semenanjung Malaysia",
                description = "Berpotensi mengeluarkan pengeluaran getah awal yang sangat rancak. Bertindak balas cemerlang kepada rangsangan kimia susu getah. Sensitif kepada angin kencang.",
                soilSuitability = "Tanah Aluvium & Tanah Liat Kompak",
                annualRainfallNeeded = "1,800 - 2,400 mm",
                maxElevation = "200 meter"
            ),
            CloneInfo(
                id = "rrim_3001",
                name = "RRIM 3001",
                series = "RRIM 3000 Series",
                yearIntroduced = "2011",
                yieldPotential = "2,800 - 3,200 kg/ha/tahun",
                diseaseResistance = "Sangat Tinggi (Sangat imun terhadap semua jenis kulat daun)",
                suggestedArea = "Kawasan Lembah, Cerun Bukit Rendah, Seluruh Malaysia",
                description = "Antara klon termoden RISDA dengan struktur kanopi kuat dan potensi hasil lateks tertinggi dalam ujian RISDA 2025. Batangnya tegak lurus mengurangkan risiko tumbang dipukul ribut.",
                soilSuitability = "Sesuai hampir semua jenis tanah (Tanah Campuran & Liat Merah)",
                annualRainfallNeeded = "2,200 - 3,200 mm",
                maxElevation = "500 meter"
            ),
            CloneInfo(
                id = "rrim_600",
                name = "RRIM 600",
                series = "RRIM Modern Heritage",
                yearIntroduced = "1956",
                yieldPotential = "1,500 - 1,800 kg/ha/tahun",
                diseaseResistance = "Tinggi terhadap gangguan fros, Sederhana Rendah terhadap Colletotrichum",
                suggestedArea = "Kawasan Hilly / Bukit yang terlindung dari angin kencang",
                description = "Klon warisan klasik RISDA. Walaupun potensi hasilnya di bawah siri 2000, RRIM 600 mempamerkan kestabilan luar biasa sepanjang hayat eksploitasi susu getah melebihi 25 tahun.",
                soilSuitability = "Tanah Berbatu (Cobbly Clay) & Tanah Bukit",
                annualRainfallNeeded = "1,500 - 2,000 mm",
                maxElevation = "400 meter"
            ),
            CloneInfo(
                id = "pr_255",
                name = "PR 255",
                series = "PR Series",
                yearIntroduced = "1978",
                yieldPotential = "1,800 - 2,200 kg/ha/tahun",
                diseaseResistance = "Tinggi (Tahan Angin & Patah Dahan)",
                suggestedArea = "Kawasan pantai barat utara Malaysia",
                description = "Ciri ketahanan daun yang cemerlang terhadap penyakit musim hujan. Sesuai untuk pekebun kecil di kawasan kerap menerima cuaca ribut tropika.",
                soilSuitability = "Tanah Pasir Berhumus & Tanah Gambut Ringan",
                annualRainfallNeeded = "1,600 - 2,200 mm",
                maxElevation = "250 meter"
            )
        )

        fun getCloneById(id: String): CloneInfo? {
            return defaultClones.find { it.id == id || it.name.replace(" ", "_").lowercase() == id.lowercase() }
        }

        fun findBestOfflineMatch(imageLabel: String): CloneInfo {
            val normalized = imageLabel.lowercase()
            return defaultClones.find { 
                normalized.contains(it.name.lowercase()) || 
                normalized.contains(it.id.lowercase().replace("_", "")) 
            } ?: defaultClones[0]
        }
    }
}
