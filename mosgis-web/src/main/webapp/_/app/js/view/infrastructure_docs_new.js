define ([], function () {

    return function (data, view) {

        var name = 'infrastructure_docs_new_form'

        $(view).w2popup('open', {

            width  : 605,
            height : 280,

            title   : 'Добавление документа',

            onOpen: function (event) {

                event.onComplete = function () {

                    if (w2ui [name]) w2ui [name].destroy ()

                    $('#w2ui-popup .w2ui-form').w2form ({

                        name: name,
                        
                        record: data.record,

                        fields : [
                            {name: 'id_type', type: 'list', options: {items: data.vc_infrastructure_file_types.items}},
                            {name: 'label', type: 'text'},
                            {name: 'description',  type: 'textarea' },
                            {name: 'files', type: 'file', options: {max: 1, maxWidth: 400}},
                        ],

                        onChange: function (e) {
                        
                            if (e.target == "files") {
                                if (e.value_new[0]) {
                                    $('#label').val (e.value_new[0].name)
                                    $('#label').trigger ('change')
                                }
                            }
                            
                        },

                    });

                    clickOn ($('#w2ui-popup button'), $_DO.update_infrastructure_docs_new)

                }

            }

        });

    }

});