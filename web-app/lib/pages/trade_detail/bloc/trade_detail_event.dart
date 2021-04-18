import 'package:cordatradeclient/models/trade_state_model.dart';
import 'package:equatable/equatable.dart';

abstract class TradeDetailEvent extends Equatable {
  @override
  List<Object?> get props => [];
}

class LoadTradeDetailEvent extends TradeDetailEvent {
  final TradeStateModel model;

  LoadTradeDetailEvent(this.model);
}

class LoadTradeDetailByIdEvent extends TradeDetailEvent {
  final String id;

  LoadTradeDetailByIdEvent(this.id);
}

class TradeDetailMarkInProcessEvent extends TradeDetailEvent {}

class TradeDetailMarkSettledEvent extends TradeDetailEvent {}
