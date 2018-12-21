define ([], function () {

    return function (data, view) {

        var is_popup = $_SESSION.delete ('licenses_popup.on')
    
        data = $('body').data ('data')

        $((w2ui ['popup_layout'] || w2ui ['rosters_layout']).el ('main')).w2regrid ({ 
            
            //todo        
            name: 'licenses_grid',

            show: {
                toolbar: true,
                footer: true,
            },     

            searches: [            
//                {field: 'ogrn',      caption: 'ОГРН(ИП)',            type: 'text', operator: 'is', operators: ['is']},
                  {field: 'licensegiud',  caption: 'Наименование',        type: 'text'},
//                {field: 'inn',       caption: 'ИНН',                 type: 'text', operator: 'is', operators: ['is']},
//                {field: 'kpp',       caption: 'КПП',                 type: 'text', operator: 'is', operators: ['is']},
//                {field: 'code_vc_nsi_20', caption: 'Полномочия',     type: 'enum', options: {items: data.vc_nsi_20.items}},
//                {field: 'id_type', caption: 'Типы',     type: 'enum', options: {items: data.vc_licenses_types.items}},
            ],

            columns: [                
                {field: 'licensegiud', caption: 'Статус',    size: 20},
                {field: 'licensegiud', caption: 'Лицензиат',    size: 100},
                {field: 'licensegiud', caption: 'Лицензирующий орган',    size: 40},
                {field: 'licensegiud', caption: 'Лицензируемый вид деятельности',    size: 100},
                {field: 'licensegiud', caption: 'Адрес осуществления лицензируемого вида деятельности',    size: 100},
            ],
//            url: '/mosgis/_rest/?type=voc_organizations',
            url: '/mosgis/_rest/?type=licenses',
            
            onDblClick: function (e) {

                var r = this.get (e.recid)

                if (is_popup) {                

                    $_SESSION.set ('licenses_popup.data', clone (r))

                    w2popup.close ()                

                }
                else {

                    function show (postfix) {openTab ('/licenses_' + postfix + '/' + r.uuid)}

//                    switch (String (r.ogrn).length) {
//                        case 13: return show ('legal')
//                        case 15: return show ('individual')
//                    }

                }

            },
            
            onRefresh: function (e) {
                                        
                e.done (function () {

                    if (this.searchData.length > 0 && this.records.length == 0) {
                                            
                        if ($_SESSION.delete ('importing')) {
                            w2alert ('Запрос в ГИС ЖКХ не дал результатов. Вероятно, Вы опечатались, вводя ОГРН[ИП]')
                        }
                        else if ($_SESSION.delete ('first_post')) {
                            $_DO.check_licenses (e)
                        }
                        else {
                            $_SESSION.set ('first_post', 1)
                        }

                    }                

                    $('#tb_licenses_grid_toolbar_item_w2ui-search .w2ui-search-all').attr ({
                        style: 'width: 160px !important',
                        placeholder: 'Все поля',
                        title: 'Все поля',
                    })
                    
                    $('#tb_licenses_grid_toolbar_item_w2ui-search .w2ui-toolbar-search').attr ({
                        style: 'width: 160px !important',
                    })

                })

            },

        }).refresh ();

    }

})