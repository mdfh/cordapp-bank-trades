import 'package:cordatradeclient/pages/dialog/bloc/dialog_state.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import 'dialog_event.dart';

class DialogBloc extends Bloc<DialogEvent, DialogState> {
  DialogBloc() : super(DialogDefaultState());

  @override
  Stream<DialogState> mapEventToState(DialogEvent event) async* {
    if (event is DialogSuccessEvent)
      yield DialogSuccessState(event.message);
    else if (event is DialogErrorEvent)
      yield DialogSuccessState(event.message);
    else if (event is DialogResetEvent) yield DialogDefaultState();
  }
}
