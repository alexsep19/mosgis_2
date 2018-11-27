define ([], function () {

    return function (data, view) {
    
        var name = 'voting_protocol_docs_new_form'

        var contract = data.contract || data    

        $(view).w2popup('open', {

            width  : 605,
            height : 215,

            title   : 'Добавление документа',

            onOpen: function (event) {

                event.onComplete = function () {

                    if (w2ui [name]) w2ui [name].destroy ()

                    $('#w2ui-popup .w2ui-form').w2form ({

                        name: name,

                        fields : [
                            {name: 'description',  type: 'textarea' },
                            {name: 'files', type: 'file'},    
                        ],

                    });

                    clickOn ($('#w2ui-popup button'), $_DO.update_voting_protocol_docs_new)

                }

            }

        });

    }

});