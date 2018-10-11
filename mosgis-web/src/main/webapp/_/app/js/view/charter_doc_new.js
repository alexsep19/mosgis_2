define ([], function () {

    return function (data, view) {
    
        var name = 'charter_doc_new_form'

        var contract = data.contract || data
                
        $(view).w2popup('open', {

            width  : 605,
            height : 250,

            title   : 'Добавление документа',

            onOpen: function (event) {

                event.onComplete = function () {

                    if (w2ui [name]) w2ui [name].destroy ()

                    $('#w2ui-popup .w2ui-form').w2form ({

                        name: name,

                        fields : [
                            {name: 'description',  type: 'textarea' },
                            {name: 'files', type: 'file', options: {max: 1}},
                            {name: 'id_type', type: 'list', options: {items: data.vc_contract_doc_types.items}},
                        ],                       
                        
                    });

                    clickOn ($('#w2ui-popup button'), $_DO.update_charter_doc_new)

                }

            }

        });

    }

});