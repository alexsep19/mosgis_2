define ([], function () {

    return function (data, view) {
    
        var name = 'voting_protocol_docs_new_form' 

        $(view).w2popup('open', {

            width  : 605,
            height : 245,

            title   : 'Добавление документа',

            onOpen: function (event) {

                event.onComplete = function () {

                    if (w2ui [name]) w2ui [name].destroy ()

                    $('#w2ui-popup .w2ui-form').w2form ({

                        name: name,

                        fields : [
                            {name: 'label', type: 'text'},
                            {name: 'description',  type: 'textarea' },
                            {name: 'files', type: 'file', options: {max: 1, maxWidth: 400}},
                        ],

                        onChange: function (e) {
                            if (e.target == "files") {
                                name = e.value_new[0].name
                                $('#label').val (name)
                            }
                        },

                    });

                    clickOn ($('#w2ui-popup button'), $_DO.update_voting_protocol_docs_new)

                }

            }

        });

    }

});