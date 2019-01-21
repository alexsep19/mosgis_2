define ([], function () {

    return function (data, view) {

        $(fill (view, {})).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: 'mgmt_contract_objects_import_popup_form',

                fields : [                                
                    {name: 'files', type: 'file', options: {max: 1, maxWidth: 290}},
                ],

            })
            
            $('.w2ui-popup a').first ().attr ({href: sessionStorage.getItem ('staticRoot') + "/libs/mosgis/Шаблон файла импорта объектов управления в ДУ.xlsx"})
            $('.w2ui-popup a').last ().attr ({href: sessionStorage.getItem ('staticRoot') + "/libs/mosgis/Описание полей файла импорта объектов управления в ДУ.docx"})
            
       })       

    }

})