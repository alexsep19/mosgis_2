define ([], function () {

    return function (data, view) {

        $(view).w2popup ('open', {

            width  : 605,
            height : 245,

            title   : 'Редактирование документа',

            onOpen: function (event) {

                event.onComplete = function () {
                                
                    $('#file_label').text (data.label)                    

                    var name = 'infrastructure_docs_popup_form'

                    if (w2ui [name]) w2ui [name].destroy ()

                    $('#w2ui-popup .w2ui-form').w2reform ({

                        name: name,

                        record: data,

                        fields : [
                            {name: 'id_type', type: 'list', options: {items: $('body').data ('data').vc_infrastructure_file_types.items}},
                            {name: 'description',  type: 'textarea' }
                        ],
                        
                        focus: -1

                    });

                    clickOn ($('#w2ui-popup button'), $_DO.update_infrastructure_docs_popup)
                    clickOn ($('#file_label'), $_DO.download_infrastructure_docs_popup)

                }

            }

        })

    }    

});