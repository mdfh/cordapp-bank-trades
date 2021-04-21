import 'package:equatable/equatable.dart';

abstract class HomeEvent extends Equatable
{
  @override
  List<Object?> get props => [];
}

class LoadHomeEvent extends HomeEvent {
  LoadHomeEvent();
}

class HomeEventIssueTradeEvent extends HomeEvent {
  final int amount;

  HomeEventIssueTradeEvent(this.amount);
}

class HomeEventSelectPeerEvent extends HomeEvent {
  final String selectedPeer;

  HomeEventSelectPeerEvent(this.selectedPeer);
}