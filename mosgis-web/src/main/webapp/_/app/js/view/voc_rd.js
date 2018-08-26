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
                    {type: 'button', caption: 'Импорт справочника из ГИС РД...', onClick: $_DO.import_voc_rd},
                    {type: 'button', caption: 'Синхронизация наборов полей', onClick: function (e) {
                        this.show ('w2ui-save')                                                
                        $('.w2ui-grid-records tr').toggleClass ('status_warning', true)
                    }},
                ],
            },

            show: {
                toolbar: true,
                footer: true,
                toolbarInput: false,
                toolbarSave: true,
            },

            url: '/mosgis/_rest/?type=voc_rd_list&part=lines&id=' + data.id,

            columns: [
                {field: 'id',   caption: 'ID',                   size: 10},
                {field: 'name', caption: 'Наименование',         size: 100},
                {field: 'vc_rd_col_types.label', caption: 'Тип', size: 30},
                
                {field: 'code_vc_nsi_197',  caption: 'Показатели справочника ГИС ЖКХ', tooltip: 'Показатели справочника ГИС ЖКХ', size: 150, attr: 'data-status'

                    , render: function (i) {
                        
                        var code
                                
                        if (i.w2ui && i.w2ui.changes) code = i.w2ui.changes.code_vc_nsi_197
                        if (code && code.id) code = code.id                        
                        if (!code) code = i.code_vc_nsi_197

                        return data.vc_nsi_197 [code]
                        
                    }
                    
                    , editable: function () {

                        var ids = {}
                        
                        $.each (this.records, function () {
                            var id = this.code_vc_nsi_197
                            if (id) ids [id] = 1
                        })
                        
                        $.each (this.getChanges (), function () {
                            var id = this.code_vc_nsi_197.id
                            if (id) ids [id] = 1
                        })

                        return {
                            type: 'list', 
                            items: data.vc_nsi_197.items.filter (function (i) {return !ids [i.id]}),
                            match: 'contains',
                        }

                    }
                    
                },
                
            ],
            
            onEditField: function (e) {
                
                if (this.toolbar.get ('w2ui-save').hidden) return e.preventDefault ()
                
                var r = this.get (e.recid)
                
                if (r.cnt_nsi_codes) {
                    
                    alert ('Для этого поля выполнено сопоставление справочников. Если действительно необходимо назначить другое поле, то предварительно следует зайти в соответствующий справочник и убрать все сопоставления там')
                    
                    return e.preventDefault ()                    
                    
                }
                
            },
            
            onSave: $_DO.update_voc_rd,
            
            onDblClick: null,            
            
            onChange: $_DO.check_voc_rd,

            onRefresh: function () {
                this.toolbar.hide ('w2ui-save')
            }
            
        }).refresh ()

    }

})