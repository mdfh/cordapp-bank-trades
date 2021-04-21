import 'package:equatable/equatable.dart';

abstract class LoginState extends Equatable
{
  @override
  List<Object?> get props => [];
}

class LoginInitState extends LoginState
{

}

class LoginSuccessState extends LoginState
{
  final String me;

  LoginSuccessState(this.me);
}

class LoginLoadingState extends LoginState
{

}

class LoginErrorState extends LoginState
{

}