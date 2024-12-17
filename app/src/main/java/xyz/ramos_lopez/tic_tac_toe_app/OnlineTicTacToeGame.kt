import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import xyz.ramos_lopez.tic_tac_toe_app.GameLogic
import xyz.ramos_lopez.tic_tac_toe_app.TicTacToeGame


class OnlineTicTacToeGame(
    val gameId: String,
    val currentPlayer: String,
    private val database: FirebaseDatabase
) : GameLogic {
    companion object {
        const val BOARD_SIZE = 9
        const val HUMAN_PLAYER = 'X'
        const val COMPUTER_PLAYER = 'O'
        const val OPEN_SPOT = ' '
    }
    private var mBoard = CharArray(BOARD_SIZE) { OPEN_SPOT }
    private val gameRef = database.getReference("partidas_activas").child(gameId)
    var isMyTurn = false
    private var mySymbol: Char = OPEN_SPOT
    private var gameStateListener: GameStateListener? = null
    private var currentGame: OnlineGame? = null
    private var hostPlayer: String = currentPlayer

    init {
        gameRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val game = snapshot.getValue(OnlineGame::class.java)
                game?.let { 
                    currentGame = it
                    hostPlayer = it.player1
                    updateGameState(it)
                }
            }
            
            override fun onCancelled(error: DatabaseError) {
                Log.e("OnlineTicTacToeGame", "Error: ${error.message}")
            }
        })
    }

    fun getHostPlayer(): String = hostPlayer
    fun setGameStateListener(listener: GameStateListener) {
        gameStateListener = listener
    }

    fun getCurrentGame(): OnlineGame? = currentGame

    private fun updateGameState(game: OnlineGame) {
        isMyTurn = game.currentTurn == currentPlayer
        mySymbol = if (currentPlayer == game.player1) {
            game.player1Symbol.first()
        } else {
            game.player2Symbol.first()
        }
        val opponentSymbol = if (mySymbol == 'X') 'O' else 'X'

        game.board.forEachIndexed { index, value ->
            mBoard[index] = when(value) {
                game.player1Symbol -> if (currentPlayer == game.player1) mySymbol else opponentSymbol
                game.player2Symbol -> if (currentPlayer == game.player2) mySymbol else opponentSymbol
                else -> OPEN_SPOT
            }
        }
        gameStateListener?.onGameStateChanged()
    }


    override fun setMove(player: Char, location: Int) {
        if (!isMyTurn || location !in 0 until BOARD_SIZE || mBoard[location] != OPEN_SPOT) {
            return
        }

        currentGame?.let { game ->
            val symbol = if (currentPlayer == game.player1) game.player1Symbol else game.player2Symbol
            val nextPlayer = if (currentPlayer == game.player1) game.player2 ?: "" else game.player1
            
            val updates = mutableMapOf<String, Any>()
            updates["board/$location"] = symbol
            updates["currentTurn"] = nextPlayer

            // Update local board immediately with your symbol
            mBoard[location] = mySymbol

            gameRef.updateChildren(updates)
                .addOnSuccessListener {
                    isMyTurn = false
                    gameStateListener?.onGameStateChanged()
                }
        }
    }


    override fun clearBoard() {
        val emptyBoard = List(BOARD_SIZE) { "" }
        gameRef.child("board").setValue(emptyBoard)
    }

    override fun getBoardState(): CharArray = mBoard.clone()

    override fun setBoardState(board: CharArray) {
        mBoard = board.clone()
        val updates = board.mapIndexed { index, value ->
            "board/$index" to value.toString()
        }.toMap()
        gameRef.updateChildren(updates)
    }

    override fun getBoardValue(position: Int): Char {
        return if (position in 0 until BOARD_SIZE) mBoard[position] else OPEN_SPOT
    }

    override fun checkForWinner(): Int {
        // Comprobar filas
        for (i in 0..6 step 3) {
            if (mBoard[i] != OPEN_SPOT &&
                mBoard[i] == mBoard[i + 1] &&
                mBoard[i] == mBoard[i + 2]) {
                val winner = if (mBoard[i] == mySymbol) 2 else 3
                updateGameStatus(winner)
                return winner
            }
        }

        // Comprobar columnas
        for (i in 0..2) {
            if (mBoard[i] != OPEN_SPOT &&
                mBoard[i] == mBoard[i + 3] &&
                mBoard[i] == mBoard[i + 6]) {
                val winner = if (mBoard[i] == mySymbol) 2 else 3
                updateGameStatus(winner)
                return winner
            }
        }

        // Comprobar diagonales
        if (mBoard[0] != OPEN_SPOT &&
            mBoard[0] == mBoard[4] &&
            mBoard[0] == mBoard[8]) {
            val winner = if (mBoard[0] == mySymbol) 2 else 3
            updateGameStatus(winner)
            return winner
        }

        if (mBoard[2] != OPEN_SPOT &&
            mBoard[2] == mBoard[4] &&
            mBoard[2] == mBoard[6]) {
            val winner = if (mBoard[2] == mySymbol) 2 else 3
            updateGameStatus(winner)
            return winner
        }

        // Comprobar empate
        if (!mBoard.contains(OPEN_SPOT)) {
            updateGameStatus(1)
            return 1
        }

        return 0 // No hay ganador aún
    }

    private fun updateGameStatus(winner: Int) {
        val updates = when (winner) {
            1 -> mapOf(
                "status" to "finished",
                "winner" to "tie",
                "currentTurn" to ""  // Clear current turn
            )
            2, 3 -> mapOf(
                "status" to "finished",
                "winner" to currentPlayer,
                "currentTurn" to ""  // Clear current turn
            )
            else -> emptyMap()
        }
        gameRef.updateChildren(updates)
    }
    // These methods are not used in online game but required by interface
    override fun getDifficultyLevel(): TicTacToeGame.DifficultyLevel = 
        TicTacToeGame.DifficultyLevel.Expert

    override fun setDifficultyLevel(difficultyLevel: TicTacToeGame.DifficultyLevel) {
        // Not used in online game
    }

    override fun getComputerMove(): Int = -1 // Not used in online game
    override fun restartGame() {
        // Reset local board
        mBoard = CharArray(BOARD_SIZE) { OPEN_SPOT }
        
        // Reset game state in Firebase
        val updates = mapOf(
            "board" to List(BOARD_SIZE) { "" },
            "currentTurn" to currentGame?.player1,
            "status" to "active",
            "winner" to null
        )
        
        gameRef.updateChildren(updates)
            .addOnSuccessListener {
                gameStateListener?.onGameStateChanged()
            }
    }
}