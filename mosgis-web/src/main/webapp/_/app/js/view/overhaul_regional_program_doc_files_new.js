define ([], function () {

    return function (data, view) {

        $(fill (view, {})).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: 'overhaul_regional_program_files_popup_form',

                record: {},

                fields : [
                    {name: 'description',  type: 'textarea'},
                    {name: 'files', type: 'file', options: {max: 1, maxWidth: 290}},
                ],

            })

       })       

    }

})