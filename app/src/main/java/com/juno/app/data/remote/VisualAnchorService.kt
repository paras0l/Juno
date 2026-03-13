package com.juno.app.data.remote

data class AnchorContent(
    val word: String,
    val meaning: String,
    val funFact: String,
    val example: String
)

object VisualAnchorService {

    private val knowledgeDatabase = mapOf(
        "cat" to AnchorContent(
            word = "cat",
            meaning = "A small domesticated carnivorous mammal",
            funFact = "Cats sleep for 70% of their lives",
            example = "The cat sat on the windowsill watching birds."
        ),
        "dog" to AnchorContent(
            word = "dog",
            meaning = "A domesticated carnivorous mammal",
            funFact = "Dogs have wet noses to help absorb scent chemicals",
            example = "My dog loves to play fetch in the park."
        ),
        "car" to AnchorContent(
            word = "car",
            meaning = "A road vehicle with wheels",
            funFact = "The first car was invented in 1886",
            example = "She drove her car to the supermarket."
        ),
        "tree" to AnchorContent(
            word = "tree",
            meaning = "A tall plant with a trunk and branches",
            funFact = "Trees can communicate through underground fungal networks",
            example = "The children played under the big oak tree."
        ),
        "flower" to AnchorContent(
            word = "flower",
            meaning = "The reproductive part of a plant",
            funFact = "The oldest flower fossil is 130 million years old",
            example = "She received a beautiful bouquet of flowers."
        ),
        "book" to AnchorContent(
            word = "book",
            meaning = "A written or printed work",
            funFact = "The first printed book was the Diamond Sutra",
            example = "I love reading a good book before bed."
        ),
        "phone" to AnchorContent(
            word = "phone",
            meaning = "A device for telephone communication",
            funFact = "The first mobile phone call was made in 1973",
            example = "She answered her phone when it rang."
        ),
        "computer" to AnchorContent(
            word = "computer",
            meaning = "An electronic device for processing data",
            funFact = "The first computer weighed 30 tons",
            example = "I use my computer for work every day."
        ),
        "bird" to AnchorContent(
            word = "bird",
            meaning = "A warm-blooded egg-laying vertebrate",
            funFact = "Hummingbirds are the only birds that can fly backwards",
            example = "A bird sat on the branch singing sweetly."
        ),
        "water" to AnchorContent(
            word = "water",
            meaning = "A clear liquid essential for life",
            funFact = "Water covers 71% of the Earth's surface",
            example = "Please drink more water to stay healthy."
        ),
        "sky" to AnchorContent(
            word = "sky",
            meaning = "The region above the earth",
            funFact = "The sky appears blue because of light scattering",
            example = "Clouds floated across the blue sky."
        ),
        "sun" to AnchorContent(
            word = "sun",
            meaning = "The star at the center of our solar system",
            funFact = "The sun makes up 99.86% of our solar system's mass",
            example = "The sun rose over the mountains this morning."
        ),
        "moon" to AnchorContent(
            word = "moon",
            meaning = "The natural satellite of the earth",
            funFact = "The moon is slowly moving away from Earth",
            example = "The moon shone brightly last night."
        ),
        "mountain" to AnchorContent(
            word = "mountain",
            meaning = "A large natural elevation of the earth's surface",
            funFact = "Mount Everest grows about 4mm taller every year",
            example = "They climbed to the top of the mountain."
        ),
        "beach" to AnchorContent(
            word = "beach",
            meaning = "A sandy shore by the sea",
            funFact = "The longest beach in the world is 120 miles long",
            example = "We spent the day at the beach."
        ),
        "food" to AnchorContent(
            word = "food",
            meaning = "Any nutritious substance for the body",
            funFact = "Humans can survive weeks without food but only days without water",
            example = "Let's order some food for dinner."
        ),
        "fruit" to AnchorContent(
            word = "fruit",
            meaning = "The sweet product of a plant containing seeds",
            funFact = "Bananas are berries, but strawberries aren't",
            example = "She ate a fresh piece of fruit for breakfast."
        ),
        "vegetable" to AnchorContent(
            word = "vegetable",
            meaning = "A plant part used as food",
            funFact = "Tomatoes are technically fruits",
            example = "Eat your vegetables to stay healthy."
        ),
        "coffee" to AnchorContent(
            word = "coffee",
            meaning = "A drink made from roasted coffee beans",
            funFact = "Coffee is the second most traded commodity in the world",
            example = "I need my morning coffee to wake up."
        ),
        "tea" to AnchorContent(
            word = "tea",
            meaning = "A hot drink made from leaves",
            funFact = "Tea was discovered accidentally 2737 BC",
            example = "She enjoyed a cup of tea in the afternoon."
        ),
        "chair" to AnchorContent(
            word = "chair",
            meaning = "A piece of furniture for sitting",
            funFact = "The word 'chair' comes from the Latin 'cathedra'",
            example = "Please take a chair and sit down."
        ),
        "table" to AnchorContent(
            word = "table",
            meaning = "A piece of furniture with a flat top",
            funFact = "The oldest known table is 5,000 years old",
            example = "They sat around the table discussing plans."
        ),
        "door" to AnchorContent(
            word = "door",
            meaning = "A hinged barrier for entering a building",
            funFact = "The first indoor doors appeared in ancient Rome",
            example = "Please open the door for me."
        ),
        "window" to AnchorContent(
            word = "window",
            meaning = "An opening in a wall for light and air",
            funFact = "The word comes from the Old Norse 'vindauga'",
            example = "She looked out the window at the rain."
        ),
        "clock" to AnchorContent(
            word = "clock",
            meaning = "A device for measuring time",
            funFact = "The first mechanical clock was invented in China",
            example = "The clock struck midnight."
        ),
        "clock" to AnchorContent(
            word = "clock",
            meaning = "A device for measuring time",
            funFact = "The first mechanical clock was invented in China",
            example = "The clock struck midnight."
        ),
        "people" to AnchorContent(
            word = "person",
            meaning = "A human being",
            funFact = "Humans share 60% of their DNA with bananas",
            example = "Each person is unique in their own way."
        ),
        "person" to AnchorContent(
            word = "person",
            meaning = "A human being",
            funFact = "Humans share 60% of their DNA with bananas",
            example = "Each person is unique in their own way."
        ),
        "man" to AnchorContent(
            word = "man",
            meaning = "An adult male human",
            funFact = "The word 'man' originally meant 'human'",
            example = "The man walked down the street."
        ),
        "woman" to AnchorContent(
            word = "woman",
            meaning = "An adult female human",
            funFact = "Women blink almost twice as much as men",
            example = "The woman smiled warmly at her friend."
        ),
        "child" to AnchorContent(
            word = "child",
            meaning = "A young human being",
            funFact = "Children laugh about 400 times a day",
            example = "The child played happily in the park."
        ),
        "baby" to AnchorContent(
            word = "baby",
            meaning = "A very young child",
            funFact = "Babies have more bones than adults",
            example = "The baby slept peacefully in the crib."
        ),
        "face" to AnchorContent(
            word = "face",
            meaning = "The front of the human head",
            funFact = "The face has over 40 muscles",
            example = "She had a happy expression on her face."
        ),
        "eye" to AnchorContent(
            word = "eye",
            meaning = "The organ of vision",
            funFact = "Your eyes can distinguish 10 million colors",
            example = "She has beautiful brown eyes."
        ),
        "hand" to AnchorContent(
            word = "hand",
            meaning = "The end part of the arm",
            funFact = "Humans have unique fingerprints",
            example = "He raised his hand to answer the question."
        ),
        "street" to AnchorContent(
            word = "street",
            meaning = "A public road in a city or town",
            funFact = "The longest street in the world is 4,800 km",
            example = "Children played in the street."
        ),
        "building" to AnchorContent(
            word = "building",
            meaning = "A structure with walls and a roof",
            funFact = "The tallest building is 828 meters tall",
            example = "The building stood tall against the sky."
        ),
        "house" to AnchorContent(
            word = "house",
            meaning = "A building for human habitation",
            funFact = "The first houses were built 11,000 years ago",
            example = "They bought a new house last year."
        ),
        "bottle" to AnchorContent(
            word = "bottle",
            meaning = "A container for liquids",
            funFact = "The first plastic bottle was made in 1973",
            example = "She filled the bottle with water."
        ),
        "cup" to AnchorContent(
            word = "cup",
            meaning = "A small open container for drinking",
            funFact = "The oldest known cup is 5,000 years old",
            example = "He held a cup of hot coffee."
        ),
        "knife" to AnchorContent(
            word = "knife",
            meaning = "A tool with a sharp blade",
            funFact = "Knives are 2.5 million years old",
            example = "Use the knife to cut the bread."
        ),
        "fork" to AnchorContent(
            word = "fork",
            meaning = "An eating utensil with prongs",
            funFact = "Forks weren't common until the 18th century",
            example = "Please pass me a fork."
        ),
        "spoon" to AnchorContent(
            word = "spoon",
            meaning = "An eating utensil with a bowl",
            funFact = "The spoon is 5,000 years old",
            example = "She stirred her coffee with a spoon."
        ),
        "plate" to AnchorContent(
            word = "plate",
            meaning = "A flat dish for serving food",
            funFact = "The first plates were made of bread",
            example = "Put the food on the plate."
        ),
        "shirt" to AnchorContent(
            word = "shirt",
            meaning = "A garment for the upper body",
            funFact = "The first shirts were worn by Egyptians",
            example = "He wore a blue shirt to work."
        ),
        "pants" to AnchorContent(
            word = "pants",
            meaning = "A garment for the legs",
            funFact = "Pants were invented by nomads 3,000 years ago",
            example = "These pants are very comfortable."
        ),
        "shoe" to AnchorContent(
            word = "shoe",
            meaning = "A covering for the foot",
            funFact = "The oldest shoe is 10,000 years old",
            example = "She bought a new pair of shoes."
        ),
        "hat" to AnchorContent(
            word = "hat",
            meaning = "A covering for the head",
            funFact = "The top hat was invented in England",
            example = "He took off his hat as a greeting."
        ),
        "glasses" to AnchorContent(
            word = "glasses",
            meaning = "Frames with lenses for vision correction",
            funFact = "The first eyeglasses were invented in Italy",
            example = "She wears glasses to read."
        ),
        "bag" to AnchorContent(
            word = "bag",
            meaning = "A container made of flexible material",
            funFact = "The oldest bags were made of animal skin",
            example = "She carried a bag full of groceries."
        ),
        "box" to AnchorContent(
            word = "box",
            meaning = "A rectangular container",
            funFact = "The first cardboard box was made in 1817",
            example = "The presents were in a box."
        ),
        "ball" to AnchorContent(
            word = "ball",
            meaning = "A round object used in games",
            funFact = "The first balls were made of rocks",
            example = "The children played with a ball."
        ),
        "bicycle" to AnchorContent(
            word = "bicycle",
            meaning = "A two-wheeled vehicle",
            funFact = "The first bicycle was called a Laufmaschine",
            example = "She rode her bicycle to school."
        ),
        "train" to AnchorContent(
            word = "train",
            meaning = "A connected series of rail cars",
            funFact = "The first passenger train started in 1825",
            example = "They took the train to the city."
        ),
        "airplane" to AnchorContent(
            word = "airplane",
            meaning = "A powered aircraft",
            funFact = "The Wright brothers' first flight was 120 feet",
            example = "The airplane landed safely."
        ),
        "boat" to AnchorContent(
            word = "boat",
            meaning = "A small vessel for water travel",
            funFact = "Boats have been around for 100,000 years",
            example = "They sailed the boat across the lake."
        ),
        "fish" to AnchorContent(
            word = "fish",
            meaning = "A cold-blooded aquatic animal",
            funFact = "Fish can't close their eyes",
            example = "The fish swam in the ocean."
        ),
        "horse" to AnchorContent(
            word = "horse",
            meaning = "A large domesticated mammal",
            funFact = "Horses can sleep standing up",
            example = "The horse ran across the field."
        ),
        "cow" to AnchorContent(
            word = "cow",
            meaning = "A domesticated bovine animal",
            funFact = "Cows have best friends",
            example = "The cow grazed in the meadow."
        ),
        "sheep" to AnchorContent(
            word = "sheep",
            meaning = "A domesticated woolly mammal",
            funFact = "Sheep can recognize faces",
            example = "The sheep grazed on the hillside."
        ),
        "chicken" to AnchorContent(
            word = "chicken",
            meaning = "A domesticated bird",
            funFact = "Chickens outnumber humans 3 to 1",
            example = "The chicken crossed the road."
        ),
        "butterfly" to AnchorContent(
            word = "butterfly",
            meaning = "An insect with colorful wings",
            funFact = "Butterflies taste with their feet",
            example = "A butterfly landed on the flower."
        ),
        "bee" to AnchorContent(
            word = "bee",
            meaning = "A flying insect that makes honey",
            funFact = "Bees do a dance to tell directions",
            example = "The bee flew from flower to flower."
        ),
        "grass" to AnchorContent(
            word = "grass",
            meaning = "Green plants covering ground",
            funFact = "Grass can grow 2 feet per day",
            example = "Children played on the grass."
        ),
        "leaf" to AnchorContent(
            word = "leaf",
            meaning = "The green part of a plant",
            funFact = "Some trees have been around for thousands of years",
            example = "A falling leaf drifted to the ground."
        ),
        "cloud" to AnchorContent(
            word = "cloud",
            meaning = "A visible mass of water vapor",
            funFact = "A cumulus cloud can weigh 1 million pounds",
            example = "Clouds floated across the sky."
        ),
        "rain" to AnchorContent(
            word = "rain",
            meaning = "Water falling from clouds",
            funFact = "The wettest place gets 460 inches of rain yearly",
            example = "The rain fell softly on the roof."
        ),
        "snow" to AnchorContent(
            word = "snow",
            meaning = "Frozen precipitation",
            funFact = "No two snowflakes are exactly alike",
            example = "Snow covered the ground in winter."
        ),
        "ice" to AnchorContent(
            word = "ice",
            meaning = "Frozen water",
            funFact = "Ice is less dense than water",
            example = "She added ice to her drink."
        ),
        "fire" to AnchorContent(
            word = "fire",
            meaning = "Burning gas producing heat and light",
            funFact = "Fire has been used for 1 million years",
            example = "The fire kept them warm."
        ),
        "road" to AnchorContent(
            word = "road",
            meaning = "A way for vehicles and people",
            funFact = "The longest road is 28,000 miles",
            example = "They drove down the long road."
        ),
        "bridge" to AnchorContent(
            word = "bridge",
            meaning = "A structure spanning a gap",
            funFact = "The oldest bridge is 3,000 years old",
            example = "They crossed the bridge carefully."
        ),
        "park" to AnchorContent(
            word = "park",
            meaning = "A public green area",
            funFact = "The first public park opened in 1844",
            example = "They walked through the park together."
        ),
        "garden" to AnchorContent(
            word = "garden",
            meaning = "A plot for growing plants",
            funFact = "Gardening is good for mental health",
            example = "She grew vegetables in her garden."
        )
    )

    fun generateAnchorContent(labels: List<RecognizedLabel>): List<AnchorContent> {
        return labels.take(5).mapNotNull { label ->
            val normalizedLabel = label.label.lowercase()
            val content = knowledgeDatabase[normalizedLabel]
            
            if (content != null) {
                content.copy(word = normalizedLabel)
            } else {
                createGenericContent(label)
            }
        }
    }

    private fun createGenericContent(label: RecognizedLabel): AnchorContent {
        return AnchorContent(
            word = label.label.lowercase(),
            meaning = "A concept related to ${label.label.lowercase()}",
            funFact = "Learning about ${label.label.lowercase()} expands your vocabulary!",
            example = "I saw an interesting ${label.label.lowercase()} today."
        )
    }
}
