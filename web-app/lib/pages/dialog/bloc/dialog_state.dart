import 'package:equatable/equatable.dart';

abstract class DialogState extends Equatable {
  @override
  List<Object?> get props => [];
}

class DialogSuccessState extends DialogState {
  final String message;

  DialogSuccessState(this.message);
}

class DialogErrorState extends DialogState {
  final String message;

  DialogErrorState(this.message);
}

class DialogDefaultState extends DialogState {}
