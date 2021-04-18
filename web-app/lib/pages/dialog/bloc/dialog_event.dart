import 'package:equatable/equatable.dart';

abstract class DialogEvent extends Equatable {
  @override
  List<Object?> get props => [];
}

class DialogSuccessEvent extends DialogEvent {
  final String message;

  DialogSuccessEvent(this.message);
}

class DialogErrorEvent extends DialogEvent {
  final String message;

  DialogErrorEvent(this.message);
}

class DialogResetEvent extends DialogEvent {}
