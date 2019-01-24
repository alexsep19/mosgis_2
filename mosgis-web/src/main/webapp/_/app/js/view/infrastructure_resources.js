define ([], function () {

    return function (data, view) {

        $(w2ui ['topmost_layout'].el ('main')).w2regrid ({ 
                 
            name: 'infrastructure_resources_grid',

            show: {
                toolbar: true,
                toolbarInput: false,
                footer: true,
            },

            columnGroups: [
                {span: 1, master: true},
                {span: 1, master: true},
                {span: 1, master: true},
                {span: 4, caption: 'Присоединенная нагрузка'}
            ],

            columns: [
                {field: 'code_vc_nsi_2', caption: 'Ресурс', size: 30, voc: data.vc_nsi_2},
                {field: 'setpower', caption: 'Установленная мощность', size: 30},
                {field: 'sitingpower', caption: 'Располагаемая мощность', size: 30},
                {field: 'totalload', caption: 'Общая', size: 30},
                {field: 'industrialload', caption: 'Промышленность', size: 30},
                {field: 'socialload', caption: 'Социальная сфера', size: 30},
                {field: 'populationload', caption: 'Население', size: 30}
            ],
            url: '/mosgis/_rest/?type=infrastructure_resources'

        }).refresh ();

    }

})