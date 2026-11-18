package com.julesmeister.sangfroid.feature.profile.ui

/** Every user intent the templates can emit. Skills are identified by key. */
data class ProfileActions(
    val onBack: () -> Unit,
    val onRoleChange: (String) -> Unit,
    val onLevelSelect: (String) -> Unit,
    val onAddSkill: (String) -> Unit,
    val onRemoveSkill: (String) -> Unit,
    val onUndoRemove: () -> Unit,
    val onUndoExpired: (Long) -> Unit,
    val onChooseCv: () -> Unit,
    val onApplyCv: () -> Unit,
    val onDismissCvMessage: () -> Unit,
    val onSave: () -> Unit
)