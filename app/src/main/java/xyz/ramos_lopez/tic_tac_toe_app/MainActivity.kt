package xyz.ramos_lopez.tic_tac_toe_app

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.SharedPreferences
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper  // Add this import
import android.view.*
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.preference.PreferenceManager

class MainActivity : Activity() {

    companion object {
        private const val DIALOG_DIFFICULTY_ID = 0
        private const val DIALOG_QUIT_ID = 1
        private const val DIALOG_ABOUT_ID = 2
    }

    private lateinit var game: TicTacToeGame
    private lateinit var infoTextView: TextView
    private lateinit var boardView: BoardView
    private lateinit var soundSwitch: Switch
    private lateinit var mHandler: Handler
    private var mIsComputerTurn = false
    private var mGoFirst = TicTacToeGame.HUMAN_PLAYER

    private var humanScore = 0
    private var tieScore = 0
    private var computerScore = 0

    // Variables para reproducir sonidos
    private var mHumanMediaPlayer: MediaPlayer? = null
    private var mComputerMediaPlayer: MediaPlayer? = null

    // Preferencias para almacenar estado del sonido
    private var isSoundEnabled: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
        
        mHandler = Handler(Looper.getMainLooper())
        
        // Initialize game and views
        game = TicTacToeGame()
        infoTextView = findViewById(R.id.information)
        soundSwitch = findViewById(R.id.sound_switch)
        boardView = findViewById(R.id.board)

        if (savedInstanceState == null) {
            startNewGame()
        } else {
            savedInstanceState.getCharArray("board")?.let { board ->
                game.setBoardState(board)
                boardView.setGame(game)
                boardView.invalidate()
                updateScoreboard()
            }
            humanScore = savedInstanceState.getInt("humanScore", 0)
            computerScore = savedInstanceState.getInt("computerScore", 0)
            tieScore = savedInstanceState.getInt("tieScore", 0)
            updateScoreboard()
        }

        // Setup sound preferences and board
        setupSoundPreferences()
        boardView.setGame(game)
        boardView.setMoveListener(object : BoardView.MoveListener {
            override fun onMoveMade() {
                onHumanMove()
            }
        })
        
        updateScoreboard()
        boardView.invalidate()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        
        // Restore the game's state
        savedInstanceState.getCharArray("board")?.let { board ->
            game.setBoardState(board)
        }
        
        // Restore scores and state
        mIsComputerTurn = savedInstanceState.getBoolean("mIsComputerTurn")
        humanScore = savedInstanceState.getInt("humanScore")
        computerScore = savedInstanceState.getInt("computerScore")
        tieScore = savedInstanceState.getInt("tieScore")
        mGoFirst = savedInstanceState.getChar("mGoFirst")
        isSoundEnabled = savedInstanceState.getBoolean("soundEnabled")
        
        // Restore UI state
        infoTextView.text = savedInstanceState.getCharSequence("info")
        soundSwitch.isChecked = isSoundEnabled
        boardView.isEnabled = !mIsComputerTurn
    }

    override fun onResume() {
        super.onResume()
        if (isSoundEnabled) {
            initializeMediaPlayers()
        }
    }

    override fun onPause() {
        super.onPause()
        releaseMediaPlayers()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.apply {
            putCharArray("board", game.getBoardState())
            putBoolean("mIsComputerTurn", mIsComputerTurn)
            putInt("humanScore", humanScore)
            putInt("computerScore", computerScore)
            putInt("tieScore", tieScore)
            putCharSequence("info", infoTextView.text)
            putChar("mGoFirst", mGoFirst)
            putBoolean("soundEnabled", isSoundEnabled)
        }
    }

    /**
     * Configura las preferencias de sonido y el Switch correspondiente.
     */
    private fun setupSoundPreferences() {
        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        isSoundEnabled = prefs.getBoolean("sound_enabled", true)
        soundSwitch.isChecked = isSoundEnabled

        soundSwitch.setOnCheckedChangeListener { _, isChecked ->
            isSoundEnabled = isChecked
            prefs.edit().putBoolean("sound_enabled", isChecked).apply()
            manageMediaPlayers(isChecked)
        }
    }

    /**
     * Inicializa los MediaPlayers para los sonidos de movimiento.
     */
    private fun initializeMediaPlayers() {
        mHumanMediaPlayer = MediaPlayer.create(this, R.raw.human_move)
        mComputerMediaPlayer = MediaPlayer.create(this, R.raw.computer_move)
    }

    /**
     * Libera los recursos de los MediaPlayers.
     */
    private fun releaseMediaPlayers() {
        mHumanMediaPlayer?.release()
        mComputerMediaPlayer?.release()
        mHumanMediaPlayer = null
        mComputerMediaPlayer = null
    }

    /**
     * Maneja la inicialización o liberación de MediaPlayers según el estado del sonido.
     */
    private fun manageMediaPlayers(enable: Boolean) {
        if (enable) {
            initializeMediaPlayers()
        } else {
            releaseMediaPlayers()
        }
    }

    /**
     * Inicia un nuevo juego, reseteando el tablero y las puntuaciones si es necesario.
     */
    private fun startNewGame() {
        game.clearBoard()
        mIsComputerTurn = false
        boardView.isEnabled = true  // Ensure board is enabled at game start
        boardView.invalidate()
        infoTextView.text = getString(R.string.first_human)
        updateScoreboard()
    }

    /**
     * Actualiza el marcador en la interfaz de usuario.
     */
    private fun updateScoreboard() {
        findViewById<TextView>(R.id.human_score).text = "Human: $humanScore"
        findViewById<TextView>(R.id.tie_score).text = "Ties: $tieScore"
        findViewById<TextView>(R.id.computer_score).text = "Computer: $computerScore"
    }

    /**
     * Maneja el movimiento realizado por el humano y la respuesta de la computadora.
     */
    private fun onHumanMove() {
        if (mIsComputerTurn) return  // Ignore if it's computer's turn

        if (isSoundEnabled) {
            mHumanMediaPlayer?.start()
        }

        var winner = game.checkForWinner()
        if (winner == 0) {
            mIsComputerTurn = true  // Set computer's turn
            boardView.isEnabled = false  // Disable board during computer turn
            infoTextView.text = getString(R.string.turn_computer)
            
            mHandler.postDelayed({
                if (!isFinishing) {
                    val move = game.getComputerMove()
                    if (move != -1) {
                        game.setMove(TicTacToeGame.COMPUTER_PLAYER, move)
                        if (isSoundEnabled) {
                            mComputerMediaPlayer?.start()
                        }
                        boardView.invalidate()
                    }
                    winner = game.checkForWinner()
                    handleWinner(winner)
                    mIsComputerTurn = false
                    if (winner == 0) {  // Only re-enable if game isn't over
                        boardView.isEnabled = true  // Re-enable board for human turn
                    }
                }
            }, 1000)
        } else {
            mIsComputerTurn = false
            handleWinner(winner)
        }
    }

    /**
     * Gestiona el resultado del juego según quién haya ganado.
     */
    private fun handleWinner(winner: Int) {
        infoTextView.text = when (winner) {
            0 -> getString(R.string.turn_human)
            1 -> {
                tieScore++
                getString(R.string.result_tie)
            }
            2 -> {
                humanScore++
                getString(R.string.result_human_wins)
            }
            else -> {
                computerScore++
                getString(R.string.result_computer_wins)
            }
        }

        if (winner != 0) {
            // Deshabilitar el tablero si el juego ha terminado
            boardView.isEnabled = false
        }
        updateScoreboard()
    }

    /**
     * Crea el menú de opciones.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)
        return true
    }

    /**
     * Maneja las selecciones del menú de opciones.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.new_game -> {
                startNewGame()
                return true
            }
            R.id.ai_difficulty -> {
                showDialog(DIALOG_DIFFICULTY_ID)
                return true
            }
            R.id.quit -> {
                showDialog(DIALOG_QUIT_ID)
                return true
            }
            R.id.about -> {
                showDialog(DIALOG_ABOUT_ID)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Crea los diálogos según el ID proporcionado.
     */
    override fun onCreateDialog(id: Int): Dialog {
        val builder = AlertDialog.Builder(this)

        return when (id) {
            DIALOG_DIFFICULTY_ID -> {
                builder.setTitle(R.string.difficulty_choose)
                val levels = arrayOf(
                    getString(R.string.difficulty_easy),
                    getString(R.string.difficulty_harder),
                    getString(R.string.difficulty_expert)
                )

                // Obtener el nivel de dificultad actual
                val selected = when (game.getDifficultyLevel()) {
                    TicTacToeGame.DifficultyLevel.Easy -> 0
                    TicTacToeGame.DifficultyLevel.Harder -> 1
                    TicTacToeGame.DifficultyLevel.Expert -> 2
                }

                builder.setSingleChoiceItems(levels, selected) { dialog, item ->
                    dialog.dismiss()

                    // Establecer el nuevo nivel de dificultad
                    game.setDifficultyLevel(
                        when (item) {
                            0 -> TicTacToeGame.DifficultyLevel.Easy
                            1 -> TicTacToeGame.DifficultyLevel.Harder
                            else -> TicTacToeGame.DifficultyLevel.Expert
                        }
                    )

                    // Mostrar el nivel seleccionado
                    Toast.makeText(
                        applicationContext,
                        levels[item],
                        Toast.LENGTH_SHORT
                    ).show()
                }
                builder.create()
            }

            DIALOG_QUIT_ID -> {
                builder.setMessage(R.string.quit_question)
                    .setCancelable(false)
                    .setPositiveButton(R.string.yes) { _, _ ->
                        this@MainActivity.finish()
                    }
                    .setNegativeButton(R.string.no, null)
                builder.create()
            }

            DIALOG_ABOUT_ID -> {
                val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val layout = inflater.inflate(R.layout.about_dialog, null)
                builder.setView(layout)
                    .setPositiveButton(R.string.ok, null)
                builder.create()
            }

            else -> super.onCreateDialog(id)
        }
    }
}
