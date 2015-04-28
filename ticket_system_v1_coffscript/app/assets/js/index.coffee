$ ->
  $.get "/credential", (credentials) ->
    $.each credentials, (index, credential) ->
      $("#credentials").append $("<li>").text credential.username
      $("#credentials").append $("<li>").text credential.password
$ ->
  $.get "/persons", (persons) ->
    $.each persons, (index, person) ->
      $("#persons").append $("<li>").text person.name

