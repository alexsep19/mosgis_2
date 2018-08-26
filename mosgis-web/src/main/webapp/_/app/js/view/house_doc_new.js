define ([], function () {

    return function (data, view) {

        var contract = data.contract || data
        
        $(view).w2popup('open', {

            width  : 605,
            height : 280,

            title   : 'Добавление документа',

            onOpen: function (event) {

                event.onComplete = function () {

                    var name = 'house_doc_new_form'

                    if (w2ui [name]) w2ui [name].destroy ()

                    $('#w2ui-popup .w2ui-form').w2form ({

                        name: name,

                        fields : [
                            {name: 'no',  type: 'text' },
                            {name: 'dt',  type: 'date' },
                            {name: 'note',  type: 'textarea' },
                            {name: 'files', type: 'file', options: {max: 1}},
                            {name: 'file_type', type: 'list', options: {items: data.doc_fields.items.filter (not_off) }},
                        ],

                    });

                    clickOn ($('#w2ui-popup button'), $_DO.update_house_doc_new)

                }
                
            }
            
        });
    
    }
    
    
});