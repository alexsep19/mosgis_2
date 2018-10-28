define ([], function () {

    return function (data, view) {

        $(view).w2popup ('open', {

            width  : 200,
            height : 190,

            title   : 'Выберите тип дома',

            onOpen: function (event) {

                event.onComplete = function () {                   

                    var name = 'mgmt_contract_object_new_house_passport_form'

                    if (w2ui [name]) w2ui [name].destroy ()

                    $('#w2ui-popup .w2ui-form').w2form ({

                        name: name,

                        record: data,

                        fields : [
                            {name: 'is_condo',  type: 'radio' },
                        ],

                        focus: -1

                    });

                    clickOn ($('#w2ui-popup button'), $_DO.create_mgmt_contract_object_house_passport)

                }

            }

        })
    }

})