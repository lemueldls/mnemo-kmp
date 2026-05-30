package models

import androidx.compose.ui.graphics.Color
import shared.generated.resources.Res

object MockData {
    val subjects = listOf(
        Subject("Bridge to Advanced Mathematics", "fx", Color(0xFF3F51B5)),
        Subject("University Physics II", "nodes", Color(0xFFEF5350)),
        Subject("Data Structures", "braces", Color(0xFF7E57C2)),
        Subject("Methods of Numerical Analysis", "chart", Color(0xFF26A69A))
    )

    val reviews = listOf(
        ReviewCardData(
            date = "Tuesday, March 24",
            title = "§ Ch 7 Iterative Methods",
            content = "Goal: Solve\n\n      Ax = b\n\ngiven A, b\n\nFPI:",
            icon = "chart",
            accentColor = Color(0xFF26A69A)
        ),
        ReviewCardData(
            date = "Monday, March 30",
            title = "Inequalities",
            content = "Ther exists Z ⊆ Z such that\n• A9 Z+ is closed under both operations + and .\n• A10 ∀n ∈ Z, n ∈ Z+ or -n ∈ Z+ or n = 0 (trichotomy)",
            icon = "fx",
            accentColor = Color(0xFF3F51B5)
        ),
        ReviewCardData(
            date = "Tuesday, March 17",
            title = "#set math.mat(delim: \"[\")",
            content = "Find the QR factorization of\n\n      A = [ 4  25  0 ]\n          [ 0  0  -2 ]\n          [ 3 -25  0 ]",
            icon = "chart",
            accentColor = Color(0xFF26A69A)
        )
    )

    val tasks = listOf(
        TaskCardData(
            content = "Book: Fundamentals of Physics (11th or 10th or 9th ed.) by Halliday & Resnick, and Walker.\n\nHWs will be assigned from 10th ed.",
            backgroundColor = Color(0xFFFBE9E7)
        ),
        TaskCardData(
            content = "You should be familiar with sections 2.5 Intermediate Value Theorem, 4.1 Extreme Values, 4.2 The Mean Value Theorem, 8.7 Numerical Integration , 9.1 Euler's Method, 9.2 First order linear, 10.1 Sequences, 10.2 Series, 10.7 Power Series, 10.8 Taylor Series, 10.9 Convergence of Taylor Series, 10.10 Applications of Taylor",
            backgroundColor = Color(0xFFE0F2F1)
        ),
        TaskCardData(
            content = "WHATS GONNA BE ON THE EXAM!!\n\n1\n2\n3.1\n3.2\n3.3\n3.4\n3.6\n4.1\n4.2\n4.4\n5.1",
            backgroundColor = Color(0xFFF5F5F5)
        )
    )
}
