package xyz.ramos_lopez.tic_tac_toe_app
import android.widget.EditText
import android.text.InputType
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
// GameManager.kt
class GameManager(private val game: TicTacToeGame) {
    var isComputerTurn = false
    var goFirst = TicTacToeGame.HUMAN_PLAYER

    var humanScore = 0
    var tieScore = 0
    var computerScore = 0

    fun checkWinner(): Int = game.checkForWinner()

    fun makeComputerMove(): Int = game.getComputerMove()

    fun setMove(player: Char, position: Int) {
        game.setMove(player, position)
    }

    fun resetScores() {
        humanScore = 0
        tieScore = 0
        computerScore = 0
    }

    fun updateScores(winner: Int) {
        when (winner) {
            1 -> tieScore++
            2 -> humanScore++
            3 -> computerScore++
        }
    }
}

// SoundManager.kt
class SoundManager(private val context: Activity) {
    private var humanMediaPlayer: MediaPlayer? = null
    private var computerMediaPlayer: MediaPlayer? = null
    var isSoundEnabled: Boolean = true

    fun initialize() {
        if (isSoundEnabled) {
            initializeMediaPlayers()
        }
    }

    fun release() {
        releaseMediaPlayers()
    }

    fun playHumanSound() {
        if (isSoundEnabled) {
            humanMediaPlayer?.start()
        }
    }

    fun playComputerSound() {
        if (isSoundEnabled) {
            computerMediaPlayer?.start()
        }
    }

    private fun initializeMediaPlayers() {
        humanMediaPlayer = MediaPlayer.create(context, R.raw.human_move)
        computerMediaPlayer = MediaPlayer.create(context, R.raw.computer_move)
    }

    private fun releaseMediaPlayers() {
        humanMediaPlayer?.release()
        computerMediaPlayer?.release()
        humanMediaPlayer = null
        computerMediaPlayer = null
    }

    fun updateSoundState(enabled: Boolean) {
        isSoundEnabled = enabled
        if (enabled) {
            initializeMediaPlayers()
        } else {
            releaseMediaPlayers()
        }
    }
}

// PreferencesManager.kt
class PreferencesManager(private val context: Activity) {
    private val prefs: SharedPreferences = context.getSharedPreferences("ttt_prefs", Activity.MODE_PRIVATE)

    fun loadGameState(gameManager: GameManager, game: TicTacToeGame) {
        gameManager.humanScore = prefs.getInt("humanScore", 0)
        gameManager.computerScore = prefs.getInt("computerScore", 0)
        gameManager.tieScore = prefs.getInt("tieScore", 0)
        game.setDifficultyLevel(TicTacToeGame.DifficultyLevel.valueOf(
            prefs.getString("difficultyLevel", "Harder")!!
        ))
    }

    fun saveGameState(gameManager: GameManager, game: TicTacToeGame) {
        prefs.edit().apply {
            putInt("humanScore", gameManager.humanScore)
            putInt("computerScore", gameManager.computerScore)
            putInt("tieScore", gameManager.tieScore)
            putString("difficultyLevel", game.getDifficultyLevel().name)
            apply()
        }
    }

    fun loadSoundPreference(): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean("sound_enabled", true)
    }

    fun saveSoundPreference(enabled: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putBoolean("sound_enabled", enabled)
            .apply()
    }

    fun saveUsername(username: String) {
        prefs.edit().putString("username", username).apply()
    }

    fun loadUsername(): String? {
        return prefs.getString("username", null)
    }
}

// DialogManager.kt
class DialogManager(private val activity: MainActivity) {
    fun createDialog(id: Int, game: TicTacToeGame): Dialog {
        val builder = AlertDialog.Builder(activity)

        return when (id) {
            MainActivity.DIALOG_DIFFICULTY_ID -> createDifficultyDialog(builder, game)
            MainActivity.DIALOG_QUIT_ID -> createQuitDialog(builder)
            MainActivity.DIALOG_ABOUT_ID -> createAboutDialog(builder)
            else -> throw IllegalArgumentException("Unknown dialog id: $id")
        }
    }

    private fun createDifficultyDialog(builder: AlertDialog.Builder, game: TicTacToeGame): Dialog {
        builder.setTitle(R.string.difficulty_choose)
        val levels = arrayOf(
            activity.getString(R.string.difficulty_easy),
            activity.getString(R.string.difficulty_harder),
            activity.getString(R.string.difficulty_expert)
        )

        val selected = when (game.getDifficultyLevel()) {
            TicTacToeGame.DifficultyLevel.Easy -> 0
            TicTacToeGame.DifficultyLevel.Harder -> 1
            TicTacToeGame.DifficultyLevel.Expert -> 2
        }

        builder.setSingleChoiceItems(levels, selected) { dialog, item ->
            dialog.dismiss()
            game.setDifficultyLevel(when (item) {
                0 -> TicTacToeGame.DifficultyLevel.Easy
                1 -> TicTacToeGame.DifficultyLevel.Harder
                else -> TicTacToeGame.DifficultyLevel.Expert
            })
            Toast.makeText(activity, levels[item], Toast.LENGTH_SHORT).show()
        }
        return builder.create()
    }

    private fun createQuitDialog(builder: AlertDialog.Builder): Dialog {
        return builder.setMessage(R.string.quit_question)
            .setCancelable(false)
            .setPositiveButton(R.string.yes) { _, _ -> activity.finish() }
            .setNegativeButton(R.string.no, null)
            .create()
    }

    private fun createAboutDialog(builder: AlertDialog.Builder): Dialog {
        val inflater = activity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val layout = inflater.inflate(R.layout.about_dialog, null)
        return builder.setView(layout)
            .setPositiveButton(R.string.ok, null)
            .create()
    }
}

// MainActivity.kt
class MainActivity : Activity() {
    companion object {
        const val DIALOG_DIFFICULTY_ID = 0
        const val DIALOG_QUIT_ID = 1
        const val DIALOG_ABOUT_ID = 2
    }

    private lateinit var game: TicTacToeGame
    private lateinit var gameManager: GameManager
    private lateinit var soundManager: SoundManager
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var dialogManager: DialogManager
    private lateinit var infoTextView: TextView
    private lateinit var boardView: BoardView
    private lateinit var soundSwitch: Switch
    private lateinit var mHandler: Handler
    private lateinit var username: String
    private lateinit var usernameTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        initializeComponents()
        setupViews()
        usernameTextView = findViewById(R.id.username_text_view)
        username = preferencesManager.loadUsername() ?: ""
        if (username.isEmpty()) {
            promptForUsername()
        } else {
            updateUsernameDisplay()
        }
        if (savedInstanceState == null) {
            startNewGame()
        } else {
            restoreGameState(savedInstanceState)
        }

        setupBoardView()
        updateScoreboard()
    }

    private fun promptForUsername() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.enter_username)

        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)

        builder.setPositiveButton(R.string.ok) { _, _ ->
            username = input.text.toString()
            preferencesManager.saveUsername(username)
            updateUsernameDisplay()
        }
        builder.setNegativeButton(R.string.cancel) { dialog, _ ->
            dialog.cancel()
            finish()
        }

        builder.show()
    }

    private fun updateUsernameDisplay() {
        usernameTextView.text = getString(R.string.player_name, username)
    }

    private fun initializeComponents() {
        mHandler = Handler(Looper.getMainLooper())
        game = TicTacToeGame()
        gameManager = GameManager(game)
        soundManager = SoundManager(this)
        preferencesManager = PreferencesManager(this)
        dialogManager = DialogManager(this)

        preferencesManager.loadGameState(gameManager, game)
        soundManager.isSoundEnabled = preferencesManager.loadSoundPreference()
    }

    private fun setupViews() {
        infoTextView = findViewById(R.id.information)
        soundSwitch = findViewById(R.id.sound_switch)
        boardView = findViewById(R.id.board)

        soundSwitch.isChecked = soundManager.isSoundEnabled
        soundSwitch.setOnCheckedChangeListener { _, isChecked ->
            soundManager.updateSoundState(isChecked)
            preferencesManager.saveSoundPreference(isChecked)
        }
    }

    private fun setupBoardView() {
        boardView.setGame(game)
        boardView.setMoveListener(object : BoardView.MoveListener {
            override fun onMoveMade() {
                handleHumanMove()
            }
        })
    }

    private fun handleHumanMove() {
        if (gameManager.isComputerTurn) return

        soundManager.playHumanSound()

        var winner = gameManager.checkWinner()
        if (winner == 0) {
            handleComputerTurn()
        } else {
            gameManager.isComputerTurn = false
            handleWinner(winner)
        }
    }

    private fun handleComputerTurn() {
        gameManager.isComputerTurn = true
        boardView.isEnabled = false
        infoTextView.text = getString(R.string.turn_computer)

        mHandler.postDelayed({
            if (!isFinishing) {
                makeComputerMove()
            }
        }, 1000)
    }

    private fun makeComputerMove() {
        val move = gameManager.makeComputerMove()
        if (move != -1) {
            gameManager.setMove(TicTacToeGame.COMPUTER_PLAYER, move)
            soundManager.playComputerSound()
            boardView.invalidate()
        }

        val winner = gameManager.checkWinner()
        handleWinner(winner)
        gameManager.isComputerTurn = false

        if (winner == 0) {
            boardView.isEnabled = true
        }
    }

    private fun handleWinner(winner: Int) {
        gameManager.updateScores(winner)
        infoTextView.text = when (winner) {
            0 -> getString(R.string.turn_human)
            1 -> getString(R.string.result_tie)
            2 -> getString(R.string.result_human_wins)
            else -> getString(R.string.result_computer_wins)
        }

        if (winner != 0) {
            boardView.isEnabled = false
        }
        updateScoreboard()
    }

    private fun updateScoreboard() {
        findViewById<TextView>(R.id.human_score).text = "Human: ${gameManager.humanScore}"
        findViewById<TextView>(R.id.tie_score).text = "Ties: ${gameManager.tieScore}"
        findViewById<TextView>(R.id.computer_score).text = "Computer: ${gameManager.computerScore}"
    }

    private fun startNewGame() {
        game.clearBoard()
        gameManager.isComputerTurn = false
        boardView.isEnabled = true
        boardView.invalidate()
        infoTextView.text = getString(R.string.first_human)
        updateScoreboard()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.new_game -> startNewGame()
            R.id.ai_difficulty -> showDialog(DIALOG_DIFFICULTY_ID)
            R.id.quit -> showDialog(DIALOG_QUIT_ID)
            R.id.about -> showDialog(DIALOG_ABOUT_ID)
            R.id.reset_scores -> {
                gameManager.resetScores()
                updateScoreboard()
            }
            R.id.edit_username -> promptForUsername()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onCreateDialog(id: Int): Dialog {
        return dialogManager.createDialog(id, game)
    }

    override fun onResume() {
        super.onResume()
        soundManager.initialize()
    }

    override fun onPause() {
        super.onPause()
        soundManager.release()
    }

    override fun onStop() {
        super.onStop()
        preferencesManager.saveGameState(gameManager, game)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.apply {
            putCharArray("board", game.getBoardState())
            putBoolean("mIsComputerTurn", gameManager.isComputerTurn)
            putInt("humanScore", gameManager.humanScore)
            putInt("computerScore", gameManager.computerScore)
            putInt("tieScore", gameManager.tieScore)
            putCharSequence("info", infoTextView.text)
            putChar("mGoFirst", gameManager.goFirst)
            putBoolean("soundEnabled", soundManager.isSoundEnabled)
            putString("difficultyLevel", game.getDifficultyLevel().name)
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        restoreGameState(savedInstanceState)
    }

    private fun restoreGameState(savedInstanceState: Bundle) {
        savedInstanceState.getCharArray("board")?.let { board ->
            game.setBoardState(board)
            boardView.setGame(game)
            boardView.invalidate()
        }

        gameManager.apply {
            isComputerTurn = savedInstanceState.getBoolean("mIsComputerTurn")
            humanScore = savedInstanceState.getInt("humanScore")
            computerScore = savedInstanceState.getInt("computerScore")
            tieScore = savedInstanceState.getInt("tieScore")
            goFirst = savedInstanceState.getChar("mGoFirst")
        }

        soundManager.isSoundEnabled = savedInstanceState.getBoolean("soundEnabled")
        infoTextView.text = savedInstanceState.getCharSequence("info")
        soundSwitch.isChecked = soundManager.isSoundEnabled
        boardView.isEnabled = !gameManager.isComputerTurn

        savedInstanceState.getString("difficultyLevel")?.let { level ->
            game.setDifficultyLevel(TicTacToeGame.DifficultyLevel.valueOf(level))
        }
    }
}