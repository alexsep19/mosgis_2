define ([], function () {
                
    return function (data, view) {
                
        var layout = $(w2ui ['vocs_layout'].el ('main')).empty ().w2relayout ({

            name: 'voc_layout',

            panels: [
                {type: 'top', size: 30, content: "foo"},
                {type: 'main', size: 400},                
            ],            
            
        });
        
        fill (view, data, $(layout.el ('top')))
        
        $(layout.el ('main')).w2regrid ({
            
            name: 'rd_cols_grid_' + data.id,
            
            reorderColumns: true,
            
            toolbar: {
                items: [
                    {type: 'button', caption: 'Импорт справочника из ГИС РД...', onClick: $_DO.import_voc_rd_voc},
                    {type: 'button', caption: 'Синхронизация справочников', onClick: $_DO.open_popup_voc_rd_voc, off: data.item.code_vc_nsi_197 && !data.nsi    },
                ].filter (not_off),
            },

            show: {
                toolbar: true,
                footer: true,
                toolbarInput: false,
//                toolbarSave: true,
            },

            url: '/mosgis/_rest/?type=voc_rd_voc&part=lines&id=' + data.id,

            columns: [
                {field: 'id',   caption: 'ID',                   size: 10},
                {field: 'name', caption: 'Наименование',         size: 100},                
                {field: 'nsi', caption: 'Значение справочника ГИС ЖКХ',  size: 100, render: function (i) {return !data.nsi ? '' : data.nsi [i.nsi_code]}},
            ],
            
            onDblClick: null,            
                        
        }).refresh ()

    }

})