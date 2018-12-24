define ([], function () {

    return function (data, view) {

        $(w2ui ['rosters_layout'].el ('main')).w2regrid ({ 
                 
            name: 'licenses_grid',

            show: {
                toolbar: true,
                footer: true,
            },     

            searches: [            
//                {field: 'ogrn',      caption: 'ОГРН(ИП)',            type: 'text', operator: 'is', operators: ['is']},
                  {field: 'license_number',  caption: 'Номер лицензии',        type: 'text'},
//                {field: 'inn',       caption: 'ИНН',                 type: 'text', operator: 'is', operators: ['is']},
//                {field: 'kpp',       caption: 'КПП',                 type: 'text', operator: 'is', operators: ['is']},
//                {field: 'code_vc_nsi_20', caption: 'Полномочия',     type: 'enum', options: {items: data.vc_nsi_20.items}},
//                {field: 'id_type', caption: 'Типы',     type: 'enum', options: {items: data.vc_licenses_types.items}},
            ],

            columns: [                
                {field: 'id_status', caption: 'Статус',    size: 50, voc: data.vc_license_status},
                {field: 'org.label', caption: 'Лицензиат',    size: 100},
                {field: 'org_authority.label', caption: 'Лицензирующий орган',    size: 100},
                {field: 'licenseable_type_of_activity', caption: 'Лицензируемый вид деятельности',    size: 100},
                {field: 'fias.label', caption: 'Адрес осуществления лицензируемого вида деятельности',    size: 100},
            ],
            url: '/mosgis/_rest/?type=licenses',
            
            onDblClick: function (e) {

            //TODO

            }

        }).refresh ();

    }

})