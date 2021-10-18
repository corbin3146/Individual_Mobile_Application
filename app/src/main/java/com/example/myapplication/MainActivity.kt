package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.notkamui.keval.Keval
import kotlin.math.*

class MainActivity : AppCompatActivity() {

    //holds the current equation the user is entering
    private var equation: String = ""
    //a history of every time the user hits the equals button
    private var history: ArrayList<String> = ArrayList()
    //tracks which numpad/keyboard fragment is displayed to the user
    private var is_keyboard1: Boolean = true
    //holds the values of the variables: A, B, C to be used when user presses equals
    private var var_A: Double = 0.0
    private var var_B: Double = 0.0
    private var var_C: Double = 0.0
    //tracks whether the store button is currently active. This button modifies the behavior of the
    // variable buttons: A, B, B
    private var is_storing = false
    //the customized math parser used to evaluate the user provided equation
    private var kvl = get_custom_keval()

    //start the app
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //initialize the second keyboard fragment so it can be swapped in later
        var fragment = frag_alt_numpad_one()
        supportFragmentManager.beginTransaction().add(fragment.id,fragment)
    }

    //created the burger option menu in the upper right
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)
        return true
    }

    //handles any selections made in the burger menu
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //sorts out which option was pressed. Due to headaches, options regarding changing themes were cut
        when (item.itemId){
            R.id.opt_history -> run {
                // code taken from https://www.tutorialspoint.com/how-to-display-a-listview-in-an-android-alert-dialog
                //we need to build a popup dialogue to show the user
                val alertDialog = AlertDialog.Builder(this)
                //pulls in the row.xml file to define what is in the dialogue
                val rowList: View = layoutInflater.inflate(R.layout.row, null)
                val listView: ListView = rowList.findViewById(R.id.listView)
                //adapt the history list for the view
                val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, history)
                //put the history list into the view
                listView.adapter = adapter
                adapter.notifyDataSetChanged()
                alertDialog.setView(rowList)
                val dialog = alertDialog.create()
                //display the popup
                dialog.show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    //saves the current variables/data
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("equation", equation)
        outState.putStringArrayList("history", history)
        outState.putBoolean("is_keyboard1", is_keyboard1)
        outState.putDouble("var_A", var_A)
        outState.putDouble("var_B", var_B)
        outState.putDouble("var_C", var_C)
        outState.putBoolean("is_storing", is_storing)

    }

    //loads the current variables/data
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        equation = savedInstanceState.getString("equation")!!
        history = savedInstanceState.getStringArrayList("history")!!
        var_A = savedInstanceState.getDouble("var_A")
        var_B = savedInstanceState.getDouble("var_B")
        var_C = savedInstanceState.getDouble("var_C")

        //sets the store key things
        if(savedInstanceState.getBoolean("is_storing")){
            is_storing = true
            //if store key is active, reduce it's alpha to notify user
            findViewById<Button>(R.id.btn_store).alpha = 0.2F
        }else{
            is_storing = false
        }

        is_keyboard1 = savedInstanceState.getBoolean("is_keyboard1")
        //make sure thet the calculator displays the current function
        findViewById<TextView>(R.id.txt_display).text = equation


    }

    //clears out the equation
    fun clear(v: View){
        equation = ""
        findViewById<TextView>(R.id.txt_display).text = equation
    }
    //deletes the last char in the equation
    fun backspace(v: View){
        equation = equation.dropLast(1)
        findViewById<TextView>(R.id.txt_display).text = equation
    }

    //allows/disallows current equation's value into a selected variable
    fun store_equation(v:View){
        if(!is_storing){
            is_storing = true
            findViewById<Button>(R.id.btn_store).alpha = 0.2F
        }else{
            is_storing = false
            findViewById<Button>(R.id.btn_store).alpha = 1F
        }
    }

    //if store is active, store the value of equation in a var, else type the var
    fun type_var(v:View){
        if (is_storing){
            when(v.id){
                R.id.btn_var_A -> {var_A = evaluate(equation)
                }
                R.id.btn_var_B -> var_B = evaluate(equation)
                R.id.btn_var_C -> var_C = evaluate(equation)
            }
            is_storing = false
            findViewById<Button>(R.id.btn_store).alpha = 1F
        }else{
            type(v)
        }
    }

    //pretty simple, append the corresponding symbols to the equation when a button is pressed
    fun type(v: View){

        equation += when (v.id) {
            R.id.btn_zero -> 0
            R.id.btn_one -> 1
            R.id.btn_two -> 2
            R.id.btn_three -> 3
            R.id.btn_four -> 4
            R.id.btn_five -> 5
            R.id.btn_six -> 6
            R.id.btn_seven -> 7
            R.id.btn_eight -> 8
            R.id.btn_nine -> 9
            R.id.btn_left_paren -> '('
            R.id.btn_right_paren -> ')'
            R.id.btn_plus -> '+'
            R.id.btn_divide -> '/'
            R.id.btn_multiply -> '*'
            R.id.btn_decimal -> '.'
            R.id.btn_minus -> "-"
            R.id.btn_negative -> "-"
            R.id.btn_sin -> "sin("
            R.id.btn_cos -> "cos("
            R.id.btn_tan -> "tan("
            R.id.btn_minus -> "^"
            R.id.btn_square -> "^2"
            R.id.btn_sqrt -> "sqrt("
            R.id.btn_root -> "root("
            R.id.btn_comma -> ","
            R.id.btn_natural_number -> "e"
            R.id.btn_pi -> "pi"
            R.id.btn_var_A -> "[A]"
            R.id.btn_var_B -> "[B]"
            R.id.btn_var_C -> "[C]"
            else -> print("unknown button")
        }
        //update the display
        findViewById<TextView>(R.id.txt_display).text = equation
    }
    //when the quals button is pressed
    fun equals(v: View){
        var answer = 0.0
        try {
            answer = evaluate(equation)
        }catch(ex:Exception){
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Error")
            builder.setMessage("Please evaluate the equation text for errors.")
            builder.show()
            return
        }
        //update the display
        findViewById<TextView>(R.id.txt_display).text = answer.toString()
        //save this equation and answer
        history.add("$equation = $answer")
        //match equation to the display
        equation = answer.toString()
    }

    //uses a modified Keval object to evaluate the equation
    fun evaluate(equation: String): Double {

        if (equation.equals("")){
            return 0.0
        }
        var text = equation
        //I could have used the keval library by modifying constants to make pseudo variables,
        // but this was easier to write and understand
        text = text.replace("[A]", "$var_A", false)
        text = text.replace("[B]", "$var_B", false)
        text = text.replace("[C]", "$var_C", false)
        return kvl.eval(text)
    }

    //swaps keyboard fragments
    fun swap_keyboard(v: View){
        when (is_keyboard1){
            true -> {
                supportFragmentManager.beginTransaction().replace(
                    R.id.frag_numpad,
                    frag_alt_numpad_one()
                ).commit()
            }
            false ->{
                supportFragmentManager.beginTransaction().replace(
                    R.id.frag_numpad,
                    frag_numpad()
                ).commit()
            }
        }
        is_keyboard1 = !is_keyboard1
    }

    //defines new functions for use in the calcualtor
    fun get_custom_keval():Keval{
        return Keval().withDefault()
            .withFunction("sin",1)
                { sin(it[0])}
            .withFunction("cos",1)
                { cos(it[0])}
            .withFunction("tan", 1)
                { tan(it[0])}
            .withFunction("sqrt", 1)
                { sqrt(it[0])}
            .withFunction("root", 2)
                { Math.pow(it[0],1/it[1])}

    }
}