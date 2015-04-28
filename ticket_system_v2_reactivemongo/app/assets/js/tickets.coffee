$ ->
  $.get "/schools", (tickets) ->
    $.each tickets, (index, ticket) ->
      $("#tickets").append $("<li>").text(ticket.school_number)
      $("#tickets").append $("<li>").text(ticket.school_name)
      $("#tickets").append $("<li>").text(ticket.ticket_number)
      $("#tickets").append $("<li>").text(ticket.count)