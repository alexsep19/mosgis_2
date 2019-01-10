define ([], function () {

    return function (data, view) {

        var name = 'supply_resource_contract_docs_new_form'

        $(view).w2popup('open', {

            width  : 615,
            height : 285,

            title   : 'Добавление документа',

            onOpen: function (event) {

                event.onComplete = function () {

                    if (w2ui [name]) w2ui [name].destroy ()

                    $('#w2ui-popup .w2ui-form').w2form ({

                        name: name,

                        record: data.record,

                        fields : [
                            {name: 'id_type', type: 'list', options: {items: data.vc_sr_ctr_file_types.items}},
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

                            if (e.target == "id_type") {
                                $('#proto').css ({visibility: e.value_new.id == 3 ? 'visible' : 'hidden'})
                            }

                        },

                    });

                    clickOn ($('#w2ui-popup button'), $_DO.update_supply_resource_contract_docs_new)

                }

            }

        });

    }

});