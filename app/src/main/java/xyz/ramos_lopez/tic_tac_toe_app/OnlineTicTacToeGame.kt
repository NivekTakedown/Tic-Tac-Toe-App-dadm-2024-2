import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import xyz.ramos_lopez.tic_tac_toe_app.GameLogic
import xyz.ramos_lopez.tic_tac_toe_app.TicTacToeGame


class OnlineTicTacToeGame(
    val gameId: String,
    private val currentPlayer: String,
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
    private var isMyTurn = false
    private var mySymbol: Char = OPEN_SPOT
    private var gameStateListener: GameStateListener? = null
    private var currentGame: OnlineGame? = null

    init {
        gameRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val game = snapshot.getValue(OnlineGame::class.java)
                game?.let { 
                    currentGame = it
                    updateGameState(it)
                }
            }
            
            override fun onCancelled(error: DatabaseError) {
                Log.e("OnlineTicTacToeGame", "Error: ${error.message}")
            }
        })
    }


    fun setGameStateListener(listener: GameStateListener) {
        gameStateListener = listener
    }

    private fun updateGameState(game: OnlineGame) {
        // Clear board first
        mBoard = CharArray(BOARD_SIZE) { OPEN_SPOT }
        
        // Only update non-empty cells
        game.board.forEachIndexed { index, value ->
            mBoard[index] = when(value) {
                game.player1Symbol -> HUMAN_PLAYER
                game.player2Symbol -> COMPUTER_PLAYER
                else -> OPEN_SPOT
            }
        }
        
        isMyTurn = game.currentTurn == currentPlayer
        mySymbol = if (currentPlayer == game.player1) HUMAN_PLAYER else COMPUTER_PLAYER
        gameStateListener?.onGameStateChanged()
    }


    override fun setMove(player: Char, location: Int) {
        if (!isMyTurn || location !in 0 until BOARD_SIZE || mBoard[location] != OPEN_SPOT) {
            return
        }

        currentGame?.let { game ->
            val symbol = if (currentPlayer == game.player1) game.player1Symbol else game.player2Symbol
            val nextPlayer = if (currentPlayer == game.player1) game.player2 else game.player1
            
            val updates = mutableMapOf<String, Any>()
            updates["board/$location"] = symbol
            updates["currentTurn"] = nextPlayer ?: ""

            gameRef.updateChildren(updates)
                .addOnSuccessListener {
                    mBoard[location] = player
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
        // Check for winner
        // Return: 0 for no winner, 1 for tie, 2 for HUMAN_PLAYER, 3 for COMPUTER_PLAYER
        
        // Check rows
        for (i in 0..6 step 3) {
            if (mBoard[i] != OPEN_SPOT && 
                mBoard[i] == mBoard[i + 1] && 
                mBoard[i] == mBoard[i + 2]) {
                return if (mBoard[i] == HUMAN_PLAYER) 2 else 3
            }
        }

        // Check columns
        for (i in 0..2) {
            if (mBoard[i] != OPEN_SPOT && 
                mBoard[i] == mBoard[i + 3] && 
                mBoard[i] == mBoard[i + 6]) {
                return if (mBoard[i] == HUMAN_PLAYER) 2 else 3
            }
        }

        // Check diagonals
        if (mBoard[0] != OPEN_SPOT && 
            mBoard[0] == mBoard[4] && 
            mBoard[0] == mBoard[8]) {
            return if (mBoard[0] == HUMAN_PLAYER) 2 else 3
        }
        
        if (mBoard[2] != OPEN_SPOT && 
            mBoard[2] == mBoard[4] && 
            mBoard[2] == mBoard[6]) {
            return if (mBoard[2] == HUMAN_PLAYER) 2 else 3
        }

        // Check for tie
        if (!mBoard.contains(OPEN_SPOT)) {
            return 1
        }

        return 0 // No winner yet
    }

    // These methods are not used in online game but required by interface
    override fun getDifficultyLevel(): TicTacToeGame.DifficultyLevel = 
        TicTacToeGame.DifficultyLevel.Expert

    override fun setDifficultyLevel(difficultyLevel: TicTacToeGame.DifficultyLevel) {
        // Not used in online game
    }

    override fun getComputerMove(): Int = -1 // Not used in online game
}