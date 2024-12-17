package xyz.ramos_lopez.tic_tac_toe_app//imports
import OnlineGame
import android.util.Log
import com.google.firebase.database.*


class OnlineGameManager(private val database: FirebaseDatabase) {

    private val gamesRef = database.getReference("partidas_activas")

    fun createGame(player1: String, callback: (String) -> Unit) {
        val gameId = gamesRef.push().key ?: return
        val game = OnlineGame(
            gameId = gameId,
            player1 = player1,
            currentTurn = player1,
            status = "waiting",
            board = List(9) { "" },
            player1Symbol = "X",
            player2Symbol = "O"
        )
        
        gamesRef.child(gameId).setValue(game)
            .addOnSuccessListener { callback(gameId) }
    }

    fun joinGame(gameId: String, player2: String, callback: (Boolean) -> Unit) {
        gamesRef.child(gameId).get().addOnSuccessListener { snapshot ->
            val game = snapshot.getValue(OnlineGame::class.java)
            if (game?.status == "waiting") {
                gamesRef.child(gameId).updateChildren(mapOf(
                    "player2" to player2,
                    "status" to "active",
                    "currentTurn" to game.player1 // Ensure player1 starts
                )).addOnSuccessListener { callback(true) }
            } else {
                callback(false)
            }
        }
    }

    fun getAvailableGames(callback: (List<OnlineGame>) -> Unit) {
        gamesRef.orderByChild("status")
            .equalTo("waiting")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val games = snapshot.children.mapNotNull { 
                        it.getValue(OnlineGame::class.java) 
                    }
                    callback(games)
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(emptyList())
                }
            })
    }
    fun getGameState(gameId: String, callback: (OnlineGame) -> Unit) {
        gamesRef.child(gameId).get().addOnSuccessListener { snapshot ->
            snapshot.getValue(OnlineGame::class.java)?.let { callback(it) }
        }
    }
    fun makeMove(gameId: String, position: Int, player: String, callback: (Boolean) -> Unit) {
        gamesRef.child(gameId).get().addOnSuccessListener { snapshot ->
            val game = snapshot.getValue(OnlineGame::class.java)
            if (game?.currentTurn == player && game.board[position].isEmpty()) {
                val symbol = if (player == game.player1) "X" else "O"
                val updates = hashMapOf<String, Any>(
                    "board/$position" to symbol,
                    "currentTurn" to if (player == game.player1) game.player2!! else game.player1
                )
                gamesRef.child(gameId).updateChildren(updates)
                    .addOnSuccessListener { callback(true) }
                    .addOnFailureListener { callback(false) }
            } else {
                callback(false)
            }
        }
    }

    fun listenForGameChanges(gameId: String, callback: (OnlineGame) -> Unit) {
        gamesRef.child(gameId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.getValue(OnlineGame::class.java)?.let { callback(it) }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("OnlineGameManager", "Error listening for game changes: ${error.message}")
            }
        })
    }

    fun checkWinner(board: List<String>): String? {
        // Check rows
        for (i in 0..6 step 3) {
            if (board[i].isNotEmpty() && board[i] == board[i + 1] && board[i] == board[i + 2]) {
                return board[i]
            }
        }

        // Check columns
        for (i in 0..2) {
            if (board[i].isNotEmpty() && board[i] == board[i + 3] && board[i] == board[i + 6]) {
                return board[i]
            }
        }

        // Check diagonals
        if (board[0].isNotEmpty() && board[0] == board[4] && board[0] == board[8]) {
            return board[0]
        }
        if (board[2].isNotEmpty() && board[2] == board[4] && board[2] == board[6]) {
            return board[2]
        }

        return null
    }

    fun endGame(gameId: String, winner: String) {
        gamesRef.child(gameId).updateChildren(mapOf(
            "status" to "finished",
            "winner" to winner
        ))
    }

    fun leaveGame(gameId: String) {
        gamesRef.child(gameId).removeValue()
    }
}