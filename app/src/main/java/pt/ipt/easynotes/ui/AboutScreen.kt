package pt.ipt.easynotes.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import pt.ipt.easynotes.R

@Composable
fun AboutScreen(
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = stringResource(R.string.about),
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        Text(
            text = stringResource(R.string.course)
        )

        Text(
            text = stringResource(R.string.subject)
        )

        Text(
            text = stringResource(R.string.school_year)
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        Text(
            text = stringResource(R.string.author),
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = stringResource(R.string.author_number)
        )

        Text(
            text = stringResource(R.string.author_name)
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        Text(
            text = stringResource(R.string.technologies),
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = stringResource(R.string.technology_kotlin)
        )

        Text(
            text = stringResource(R.string.technology_compose)
        )

        Text(
            text = stringResource(R.string.technology_room)
        )

        Text(
            text = stringResource(R.string.technology_navigation)
        )

        Spacer(
            modifier = Modifier.weight(1f)
        )

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.back)
            )
        }
    }
}