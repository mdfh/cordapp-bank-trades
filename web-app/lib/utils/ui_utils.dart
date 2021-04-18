import 'package:flutter/material.dart';

class UiErrorUtils {
  // opens snackbar
  void openSnackBar(BuildContext context, String message,
      {VoidCallback? onClosed}) async {
    ScaffoldMessenger.of(context)
        .showSnackBar(
          SnackBar(
            content: Text(message),
          ),
        )
        .closed
        .then((reason) {
      onClosed?.call();
    });
  }
}
