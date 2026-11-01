package com.darkz.calculator.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.darkz.calculator.ui.theme.*

@Composable
fun CalculatorScreen() {
    var expression by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("0") }

    fun onButtonClick(value: String) {
        when (value) {
            "AC" -> {
                expression = ""
                result = "0"
            }
            "⌫" -> {
                if (expression.isNotEmpty()) {
                    expression = expression.dropLast(1)
                }
            }
            "=" -> {
                if (expression.isNotEmpty()) {
                    try {
                        val evaluated = evaluateExpression(expression)
                        result = formatResult(evaluated)
                    } catch (e: Exception) {
                        result = "Erro"
                    }
                }
            }
            else -> {
                // Prevent multiple consecutive operators or handle inputs
                expression += value
                try {
                    val evaluated = evaluateExpression(expression)
                    result = formatResult(evaluated)
                } catch (e: Exception) {
                    // Ignore live evaluation errors while typing incomplete expressions
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // App Title Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "DARKZ",
                color = WhiteText,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "CALCULATOR",
                color = RedPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        }

        // Display Screen
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface, RoundedCornerShape(20.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = expression.ifEmpty { "0" },
                color = GrayText,
                fontSize = 24.sp,
                maxLines = 2,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = result,
                color = WhiteText,
                fontSize = 44.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Keypad Grid
        val buttons = listOf(
            listOf("AC", "⌫", "%", "÷"),
            listOf("7", "8", "9", "×"),
            listOf("4", "5", "6", "-"),
            listOf("1", "2", "3", "+"),
            listOf("00", "0", ",", "=")
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            for (row in buttons) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    for (btn in row) {
                        val isOperator = btn in listOf("÷", "×", "-", "+", "=")
                        val isAction = btn in listOf("AC", "⌫", "%")

                        val buttonColor = when {
                            btn == "=" -> RedPrimary
                            isOperator -> RedDark
                            isAction -> GrayButton
                            else -> DarkerSurface
                        }

                        val textColor = when {
                            btn == "=" -> WhiteText
                            isOperator -> RedLight
                            isAction -> WhiteText
                            else -> WhiteText
                        }

                        Button(
                            onClick = {
                                val mappedValue = when (btn) {
                                    "÷" -> "/"
                                    "×" -> "*"
                                    "," -> ","
                                    else -> btn
                                }
                                onButtonClick(mappedValue)
                            },
                            modifier = Modifier
                                .weight(if (btn == "00") 2f else 1f)
                                .height(75.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = buttonColor,
                                contentColor = textColor
                            )
                        ) {
                            Text(
                                text = btn,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

// Simple and safe arithmetic expression evaluator supporting +, -, *, /, %, decimal comma
fun evaluateExpression(expr: String): Double {
    // Replace comma with dot, and formatting symbols
    val sanitized = expr.replace(",", ".").replace("×", "*").replace("÷", "/")
    
    // Parse basic tokens (numbers and operators)
    val tokens = mutableListOf<String>()
    var currentNumber = StringBuilder()

    for (char in sanitized) {
        if (char in "+-*/%") {
            if (currentNumber.isNotEmpty()) {
                tokens.add(currentNumber.toString())
                currentNumber = StringBuilder()
            }
            tokens.add(char.toString())
        } else {
            currentNumber.append(char)
        }
    }
    if (currentNumber.isNotEmpty()) {
        tokens.add(currentNumber.toString())
    }

    if (tokens.isEmpty()) return 0.0

    // Handle percentage (%) if any
    val processedTokens = mutableListOf<String>()
    var i = 0
    while (i < tokens.size) {
        if (tokens[i] == "%") {
            if (processedTokens.isNotEmpty()) {
                val lastNum = processedTokens.removeLast().toDoubleOrNull() ?: 0.0
                processedTokens.add((lastNum / 100.0).toString())
            }
        } else {
            processedTokens.add(tokens[i])
        }
        i++
    }

    // First pass: multiplication and division
    val pass1 = mutableListOf<String>()
    i = 0
    while (i < processedTokens.size) {
        val token = processedTokens[i]
        if (token == "*" || token == "/") {
            val prev = pass1.removeLast().toDouble()
            val next = processedTokens[i + 1].toDouble()
            val res = if (token == "*") prev * next else if (next != 0.0) prev / next else throw ArithmeticException("Div by zero")
            pass1.add(res.toString())
            i++
        } else {
            pass1.add(token)
        }
        i++
    }

    // Second pass: addition and subtraction
    var result = pass1[0].toDouble()
    i = 1
    while (i < pass1.size) {
        val op = pass1[i]
        val next = pass1[i + 1].toDouble()
        if (op == "+") result += next
        if (op == "-") result -= next
        i += 2
    }

    return result
}

fun formatResult(value: Double): String {
    return if (value == value.toLong().toDouble()) {
        value.toLong().toString().replace(".", ",")
    } else {
        value.toString().replace(".", ",")
    }
}
