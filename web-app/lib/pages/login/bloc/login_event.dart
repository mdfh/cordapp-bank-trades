import 'package:equatable/equatable.dart';

abstract class LoginEvent extends Equatable
{
  @override
  List<Object?> get props => [];
}

class LoginInitEvent extends LoginEvent
{
  final String domain, port, username, password;

  LoginInitEvent(this.domain, this.port, this.username, this.password);
}