define ([], function () {

    return function (data, view) {

        var is_popup = $_SESSION.delete ('voc_licenses_popup.on')
    
        data = $('body').data ('data')

        $((w2ui ['popup_layout'] || w2ui ['rosters_layout']).el ('main')).w2regrid ({ 

            name: 'voc_licenses_grid',

            show: {
                toolbar: true,
                footer: true,
            },     

            searches: [            
//                {field: 'ogrn',      caption: 'ОГРН(ИП)',            type: 'text', operator: 'is', operators: ['is']},
                  {field: 'licensenumber',  caption: 'Наименование',        type: 'text'},
//                {field: 'inn',       caption: 'ИНН',                 type: 'text', operator: 'is', operators: ['is']},
//                {field: 'kpp',       caption: 'КПП',                 type: 'text', operator: 'is', operators: ['is']},
//                {field: 'code_vc_nsi_20', caption: 'Полномочия',     type: 'enum', options: {items: data.vc_nsi_20.items}},
//                {field: 'id_type', caption: 'Типы',     type: 'enum', options: {items: data.vc_licenses_types.items}},
            ],

            columns: [                
                {field: 'label', caption: 'Статус',    size: 20},
                {field: 'label', caption: 'Лицензиат',    size: 100},
                {field: 'label', caption: 'Лицензирующий орган',    size: 40},
                {field: 'label', caption: 'Лицензируемый вид деятельности',    size: 100},
                {field: 'label', caption: 'Адрес осуществления лицензируемого вида деятельности',    size: 100},
            ],

            url: '/mosgis/_rest/?type=voc_licenses',
            
            onDblClick: function (e) {

                var r = this.get (e.recid)

                if (is_popup) {                

                    $_SESSION.set ('voc_licenses_popup.data', clone (r))

                    w2popup.close ()                

                }
                else {

                    function show (postfix) {openTab ('/voc_licenses_' + postfix + '/' + r.uuid)}

                    switch (String (r.ogrn).length) {
                        case 13: return show ('legal')
                        case 15: return show ('individual')
                    }

                }

            },
            
            onRefresh: function (e) {
                                        
                e.done (function () {

                    if (this.searchData.length > 0 && this.records.length == 0) {
                                            
                        if ($_SESSION.delete ('importing')) {
                            w2alert ('Запрос в ГИС ЖКХ не дал результатов. Вероятно, Вы опечатались, вводя ОГРН[ИП]')
                        }
                        else if ($_SESSION.delete ('first_post')) {
                            $_DO.check_voc_licenses (e)
                        }
                        else {
                            $_SESSION.set ('first_post', 1)
                        }

                    }                

                    $('#tb_voc_licenses_grid_toolbar_item_w2ui-search .w2ui-search-all').attr ({
                        style: 'width: 450px !important',
                        placeholder: 'Введите полный ОГРН[ИП], ИНН или подстроку наименования/ФИО для поиска',
                        title: 'Указав ИНН или ОГРН, Вы можете добавить через пробел КПП.\n\nДостаточно первых двух цифр, например: 7711122233 77',
                    })
                    
                    $('#tb_voc_licenses_grid_toolbar_item_w2ui-search .w2ui-toolbar-search').attr ({
                        style: 'width: 450px !important',
                    })

                })

            },

        }).refresh ();

    }

})