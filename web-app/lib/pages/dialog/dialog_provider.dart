import 'package:cordatradeclient/pages/dialog/bloc/dialog_bloc.dart';
import 'package:cordatradeclient/pages/dialog/bloc/dialog_event.dart';
import 'package:cordatradeclient/pages/dialog/bloc/dialog_state.dart';
import 'package:cordatradeclient/utils/ui_utils.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

class DialogProvider extends StatelessWidget {
  final Widget child;
  final DialogBloc bloc;

  const DialogProvider({Key? key, required this.bloc, required this.child})
      : super(key: key);

  @override
  Widget build(BuildContext context) {
    return BlocConsumer<DialogBloc, DialogState>(
        bloc: bloc,
        builder: (context, state) => child,
        listener: (context, state) {
          if (state is DialogSuccessState) {
            UiErrorUtils().openSnackBar(context, state.message, onClosed: () {
              bloc..add(DialogResetEvent());
            });
          } else if (state is DialogErrorState) {
            UiErrorUtils().openSnackBar(context, state.message, onClosed: () {
              bloc..add(DialogResetEvent());
            });
          }
        });
  }
}
